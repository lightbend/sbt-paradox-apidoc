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
import org.pegdown.ast.DirectiveNode.Source
import org.pegdown.ast.{DirectiveNode, Visitor}

class ApidocDirective(allClassesAndObjects: IndexedSeq[String], properties: Map[String, String])
    extends InlineDirective("apidoc") {
  final val JavadocProperty = raw"""javadoc\.(.*)\.base_url""".r
  final val JavadocBaseUrls = properties.collect {
    case (JavadocProperty(pkg), url) => pkg -> url
  }

  val allClasses = allClassesAndObjects.filterNot(_.endsWith("$"))

  private case class Query(label: Option[String], pattern: String, generics: String, linkToObject: Boolean) {
    def scalaLabel(matched: String): String =
      label match {
        case None => matched.split('.').last + generics
        case Some(la) => la + generics
      }

    def javaLabel(matched: String): String =
      scalaLabel(matched)
        .replaceAll("\\[", "<")
        .replaceAll("\\]", ">")
        .replaceAll("_", "?")

    override def toString =
      if (linkToObject) pattern + "$" + generics
      else pattern + generics
  }
  private object Query {
    def apply(label: String): Query = {
      val (pattern, generics) = splitGenerics(label)
      if (pattern.endsWith("$"))
        Query(None, pattern.init, generics, linkToObject = true)
      else
        Query(None, pattern, generics, linkToObject = false)
    }

    def apply(label: String, pattern: String): Query = {
      val (labelPattern, generics) = splitGenerics(label)
      if (pattern.endsWith("$"))
        Query(Some(labelPattern), pattern.init, generics, linkToObject = true)
      else
        Query(Some(labelPattern), pattern, generics, linkToObject = false)
    }

    private def splitGenerics(label: String): (String, String) =
      label.indexOf('[') match {
        case -1 => (label, "")
        case n => label.replaceAll("\\\\_", "_").splitAt(n)
      }
  }

  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val query = node.source match {
      case Source.Empty | _: Source.Ref => Query(node.label)
      case s: Source.Direct => Query(node.label, s.value)
    }
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

  private def syntheticNode(
      group: String,
      doctype: String,
      label: String,
      fqcn: String,
      anchor: String,
      node: DirectiveNode
  ): DirectiveNode = {
    val attributes = new org.pegdown.ast.DirectiveAttributes.AttributeMap()
    new DirectiveNode(
      DirectiveNode.Format.Inline,
      group,
      null,
      null,
      attributes,
      null,
      new DirectiveNode(
        DirectiveNode.Format.Inline,
        doctype + "doc",
        label,
        new DirectiveNode.Source.Direct(fqcn + anchor),
        node.attributes,
        label, // contents
        null
      )
    )
  }

  def renderMatches(
      query: Query,
      matches: Seq[String],
      node: DirectiveNode,
      visitor: Visitor,
      printer: Printer
  ): Unit = {
    val scalaClassSuffix = if (query.linkToObject) "$" else ""
    val sAnchor          = node.attributes.value("scala", "")
    val jAnchor          = node.attributes.value("java", "")

    matches.size match {
      case 0 =>
        throw new java.lang.IllegalStateException(s"No matches found for $query")
      case 1 if matches(0).contains("adsl") =>
        throw new java.lang.IllegalStateException(s"Match for $query only found in one language: ${matches(0)}")
      case 1 =>
        val pkg = matches(0)
        syntheticNode("scala", "scala", query.scalaLabel(pkg), pkg + scalaClassSuffix, sAnchor, node).accept(visitor)
        if (hasJavadocUrl(pkg)) {
          syntheticNode("java", "java", query.javaLabel(pkg), pkg, jAnchor, node).accept(visitor)
        } else
          syntheticNode("java", "scala", query.scalaLabel(pkg), pkg + scalaClassSuffix, jAnchor, node).accept(visitor)
      case 2 if matches.forall(_.contains("adsl")) =>
        matches.foreach(pkg => {
          if (!pkg.contains("javadsl"))
            syntheticNode("scala", "scala", query.scalaLabel(pkg), pkg + scalaClassSuffix, sAnchor, node)
              .accept(visitor)
          if (!pkg.contains("scaladsl")) {
            if (hasJavadocUrl(pkg))
              syntheticNode("java", "java", query.javaLabel(pkg), pkg, jAnchor, node).accept(visitor)
            else
              syntheticNode("java", "scala", query.scalaLabel(pkg), pkg + scalaClassSuffix, jAnchor, node)
                .accept(visitor)
          }
        })
      case n =>
        throw new java.lang.IllegalStateException(
          s"$n matches found for $query, but not javadsl/scaladsl: ${matches.mkString(", ")}. " +
              s"You may want to use the fully qualified class name as @apidoc[fqcn] instead of @apidoc[$query]."
        )
    }
  }

  /**
   * Logic borrowed from Paradox project:
   * https://github.com/lightbend/paradox/blob/b271a6bbd249515405a06df58f298a57c34afeb5/core/src/main/scala/com/lightbend/paradox/markdown/Directive.scala#L276
   */
  private def hasJavadocUrl(pkg: String) = {
    val levels     = pkg.split("[.]")
    val packages   = (1 until levels.size).map(levels.take(_).mkString("."))
    val javadocUrl = packages.reverse.collectFirst(JavadocBaseUrls)
    javadocUrl.exists(!_.isEmpty)
  }
}
