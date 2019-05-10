/*
 * Copyright 2018 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.apidoc

import com.lightbend.paradox.markdown.InlineDirective
import org.pegdown.Printer
import org.pegdown.ast.{DirectiveNode, TextNode, Visitor}

class ApidocDirective(allClassesAndObjects: IndexedSeq[String]) extends InlineDirective("apidoc") {
  val allClasses = allClassesAndObjects.filterNot(_.endsWith("$"))

  private case class Query(pattern: String, generics: String, linkToObject: Boolean) {

    def scalaLabel(matched: String): String =
      matched.split('.').last + generics
    def javaLabel(matched: String): String =
      scalaLabel(matched)
        .replaceAll("\\[", "&lt;")
        .replaceAll("\\]", "&gt;")
        .replaceAll("_", "?")

    override def toString =
      if (linkToObject) pattern + "$" + generics
      else pattern + generics
  }
  private object Query {
    def apply(label: String): Query = {
      val (pattern, generics) = label.indexOf('[') match {
        case -1 => (label, "")
        case n => label.replaceAll("\\\\_", "_").splitAt(n)
      }
      if (pattern.endsWith("$"))
        Query(pattern.init, generics, linkToObject = true)
      else
        Query(pattern, generics, linkToObject = false)
    }
  }

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val query = Query(node.label)
    if (query.pattern.contains('.')) {
      if (allClasses.contains(query.pattern)) {
        renderMatches(query, Seq(query.pattern), node, visitor, printer)
      } else
        allClasses.filter(_.contains(query.pattern)) match {
          case Seq() =>
            // No matches? then try globbing
            val regex = (query.pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*") + "$").r
            allClasses.filter(cls => regex.findFirstMatchIn(cls).isDefined) match {
              case Seq() =>
                throw new java.lang.IllegalStateException(s"Class not found for @apidoc[$query]")
              case results =>
                renderMatches(query, results, node, visitor, printer)
            }
          case results =>
            renderMatches(query, results, node, visitor, printer)
        }
    } else {
      renderMatches(query, allClasses.filter(_.endsWith('.' + query.pattern)), node, visitor, printer)
    }
  }

  def syntheticNode(group: String, label: String, fqcn: String, node: DirectiveNode): DirectiveNode = {
    val syntheticSource = new DirectiveNode.Source.Direct(fqcn)
    val attributes      = new org.pegdown.ast.DirectiveAttributes.AttributeMap()
    new DirectiveNode(
      DirectiveNode.Format.Inline,
      group,
      null,
      null,
      attributes,
      null,
      new DirectiveNode(DirectiveNode.Format.Inline,
                        group + "doc",
                        label,
                        syntheticSource,
                        node.attributes,
                        fqcn,
                        new TextNode(label))
    )
  }

  def renderMatches(query: Query,
                    matches: Seq[String],
                    node: DirectiveNode,
                    visitor: Visitor,
                    printer: Printer): Unit = {
    val scalaClassSuffix = if (query.linkToObject) "$" else ""

    matches.size match {
      case 0 =>
        throw new java.lang.IllegalStateException(s"No matches found for $query")
      case 1 if matches(0).contains("adsl") =>
        throw new java.lang.IllegalStateException(s"Match for $query only found in one language: ${matches(0)}")
      case 1 =>
        syntheticNode("scala", query.scalaLabel(matches(0)), matches(0) + scalaClassSuffix, node).accept(visitor)
        syntheticNode("java", query.javaLabel(matches(0)), matches(0), node).accept(visitor)
      case 2 if matches.forall(_.contains("adsl")) =>
        matches.foreach(m => {
          if (!m.contains("javadsl"))
            syntheticNode("scala", query.scalaLabel(m), m + scalaClassSuffix, node).accept(visitor)
          if (!m.contains("scaladsl"))
            syntheticNode("java", query.javaLabel(m), m, node).accept(visitor)
        })
      case n =>
        throw new java.lang.IllegalStateException(
          s"$n matches found for $query, but not javadsl/scaladsl: ${matches.mkString(", ")}. " +
            s"You may want to use the fully qualified class name as @apidoc[fqcn] instead of @apidoc[$query]."
        )
    }
  }

}
