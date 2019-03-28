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

class ApidocDirective(allClasses: IndexedSeq[String]) extends InlineDirective("apidoc") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit =
    if (node.label.split('[')(0).contains('.')) {
      val fqcn = node.label
      if (allClasses.contains(fqcn)) {
        val label = fqcn.split('.').last
        syntheticNode("scala", scalaLabel(label), fqcn, node).accept(visitor)
        syntheticNode("java", javaLabel(label), fqcn, node).accept(visitor)
      } else {
        throw new java.lang.IllegalStateException(s"fqcn not found by @apidoc[$fqcn]")
      }
    } else {
      renderByClassName(node.label, node, visitor, printer)
    }

  private def baseClassName(label: String) = {
    val labelWithoutGenerics = label.split("\\[")(0)
    if (labelWithoutGenerics.endsWith("$")) labelWithoutGenerics.init
    else labelWithoutGenerics
  }

  def javaLabel(label: String): String =
    scalaLabel(label).replaceAll("\\[", "&lt;").replaceAll("\\]", "&gt;").replace('_', '?')

  def scalaLabel(label: String): String =
    if (label.endsWith("$")) label.init
    else label

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

  def renderByClassName(label: String, node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val query            = node.label.replaceAll("\\\\_", "_")
    val className        = baseClassName(query)
    val scalaClassSuffix = if (query.endsWith("$")) "$" else ""

    val matches = allClasses.filter(_.endsWith('.' + className))
    matches.size match {
      case 0 =>
        throw new java.lang.IllegalStateException(s"No matches found for $query")
      case 1 if matches(0).contains("adsl") =>
        throw new java.lang.IllegalStateException(s"Match for $query only found in one language: ${matches(0)}")
      case 1 =>
        syntheticNode("scala", scalaLabel(query), matches(0) + scalaClassSuffix, node).accept(visitor)
        syntheticNode("java", javaLabel(query), matches(0), node).accept(visitor)
      case 2 if matches.forall(_.contains("adsl")) =>
        matches.foreach(m => {
          if (!m.contains("javadsl"))
            syntheticNode("scala", scalaLabel(query), m + scalaClassSuffix, node).accept(visitor)
          if (!m.contains("scaladsl"))
            syntheticNode("java", javaLabel(query), m, node).accept(visitor)
        })
      case n =>
        throw new java.lang.IllegalStateException(
          s"$n matches found for $query, but not javadsl/scaladsl: ${matches.mkString(", ")}. " +
            s"You may want to use the fully qualified class name as @apidoc[fqcn] instead of @apidoc[${label}]."
        )
    }
  }

}
