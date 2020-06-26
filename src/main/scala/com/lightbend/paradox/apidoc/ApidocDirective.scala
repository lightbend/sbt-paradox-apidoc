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

import com.lightbend.paradox.ParadoxError
import com.lightbend.paradox.ParadoxException
import com.lightbend.paradox.markdown.{Url => ParadoxUrl}
import com.lightbend.paradox.markdown.{InlineDirective, Writer}
import io.github.classgraph.ScanResult
import org.pegdown.Printer
import org.pegdown.ast.DirectiveNode.Source
import org.pegdown.ast.{DirectiveNode, Visitor}

import scala.collection.JavaConverters._

import scala.util.matching.Regex

class ApidocDirective(scanner: ScanResult, allClassesAndObjects: IndexedSeq[String], ctx: Writer.Context)
    extends InlineDirective("apidoc") {
  final val JavadocProperty = raw"""javadoc\.(.*)\.base_url""".r
  final val JavadocBaseUrls = ctx.properties.collect {
    case (JavadocProperty(pkg), url) => pkg -> url
  }

  val allClasses = allClassesAndObjects.filterNot(_.endsWith("$"))

  def containsOnlyStaticForwarders(className: String): Boolean = {
    val info = scanner.getClassInfo(className)
    info != null && info.isFinal && info.getMethodInfo.asScala.forall(_.isStatic)
  }

  private def errorForStaticForwardersOnly(query: Query, node: DirectiveNode, classname: String) =
    if (
      !query.linkToObject && containsOnlyStaticForwarders(classname) &&
      allClassesAndObjects.contains(classname + "$")
    )
      ctx.error(
        s"Class `$classname` matches @apidoc[$query], but is empty, did you intend to link to the object?",
        node
      )

  private case class Query(label: Option[String], pattern: String, generics: String, linkToObject: Boolean) {
    def scalaLabel(matched: String): String =
      label match {
        case None =>
          matched
            .split('.')
            .last
            // replace inner class dots
            .replaceAll("\\$(.)", ".$1")
            // remove ending $
            .replaceAll("\\$$", "") + generics
        case Some(la) => la + generics
      }

    def scalaFqcn(matched: String): String =
      matched.replaceAll("\\$(.)", ".$1")

    def javaLabel(matched: String): String =
      scalaLabel(matched)
        .replaceAll("\\[", "<")
        .replaceAll("\\]", ">")
        .replaceAll("_", "?")

    def javaFqcn(matched: String): String = scalaFqcn(matched)

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
      val classNameWithDollarForInnerClasses = query.pattern.replaceAll("(\\b[A-Z].+)\\.", "$1\\$")
      if (allClasses.contains(classNameWithDollarForInnerClasses))
        renderMatches(query, Seq(query.pattern), node, visitor, printer)
      else
        allClasses.filter(_.endsWith(classNameWithDollarForInnerClasses)) match {
          case Seq() =>
            // No matches? then try globbing
            val regex = convertToRegex(classNameWithDollarForInnerClasses)
            allClasses.filter(cls => regex.findFirstMatchIn(cls).isDefined) match {
              case Seq() =>
                ctx.error(s"Class not found for @apidoc[$query] (pattern $regex)", node)
              case results =>
                renderMatches(query, results, node, visitor, printer)
            }
          case results =>
            renderMatches(query, results, node, visitor, printer)
        }
    } else { // only a classname
      val className    = '.' + query.pattern
      val classMatches = allClasses.filter(_.endsWith(className))
      if (classMatches.size == 1 && classMatches(0).contains(".javadsl.")) {
        errorForStaticForwardersOnly(query, node, classMatches(0))
        val objectName = className + '$'
        val allMatches = allClassesAndObjects.filter(name => name.endsWith(className) || name.endsWith(objectName))
        renderMatches(query, allMatches, node, visitor, printer)
      } else renderMatches(query, classMatches, node, visitor, printer)
    }
  }

  private def convertToRegex(classNameWithDollarForInnerClasses: String): Regex =
    (classNameWithDollarForInnerClasses
      .replaceAll("\\.", "\\\\.")
      .replaceAll("\\*", ".*")
      .replaceAll("\\$", "\\\\\\$") + "$").r

  private def scaladocNode(
      group: String,
      label: String,
      fqcn: String,
      anchor: String,
      node: DirectiveNode
  ): DirectiveNode = syntheticNode(group, "scala", label, fqcn, anchor, node)

  private def javadocNode(
      label: String,
      fqcn: String,
      anchor: String,
      node: DirectiveNode
  ): DirectiveNode = syntheticNode("java", "java", label, fqcn, anchor, node)

  private def syntheticNode(
      group: String,
      doctype: String,
      label: String,
      fqcn: String,
      anchor: String,
      node: DirectiveNode
  ): DirectiveNode = {
    val attributes = new org.pegdown.ast.DirectiveAttributes.AttributeMap()
    val theUrl     = fqcn + anchor
    try ParadoxUrl(theUrl)
    catch {
      case ParadoxUrl.Error(reason) =>
        val suggestedUrl = theUrl
          .replace("<", "%3C")
          .replace(">", "%3E")
          .replace("[", "%5B")
        throw new ParadoxException(
          ParadoxError(
            s"$reason. Try percent-encoding manually some of the reserved characters, for example: [$suggestedUrl]. See https://github.com/lightbend/sbt-paradox-apidoc/pull/130 for more details.",
            None,
            None
          )
        )
    }
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
        new DirectiveNode.Source.Direct(theUrl),
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
        ctx.error(s"No matches found for apidoc query [$query]", node)
      case 1 if matches(0).contains("adsl") =>
        ctx.error(
          s"Match for apidoc query [$query] only found in one language: ${matches(0)}",
          node
        )
      case 1 =>
        val pkg = matches(0)
        errorForStaticForwardersOnly(query, node, query.scalaFqcn(pkg))
        scaladocNode("scala", query.scalaLabel(pkg), query.scalaFqcn(pkg) + scalaClassSuffix, sAnchor, node)
          .accept(visitor)
        if (hasJavadocUrl(pkg))
          javadocNode(query.javaLabel(pkg), query.javaFqcn(pkg), jAnchor, node).accept(visitor)
        else
          scaladocNode("java", query.javaLabel(pkg), query.scalaFqcn(pkg) + scalaClassSuffix, jAnchor, node)
            .accept(visitor)
      case 2 if matches.forall(_.contains("adsl")) =>
        matches.foreach { pkg =>
          if (!pkg.contains("javadsl")) {
            errorForStaticForwardersOnly(query, node, query.scalaFqcn(pkg))
            scaladocNode("scala", query.scalaLabel(pkg), query.scalaFqcn(pkg) + scalaClassSuffix, sAnchor, node)
              .accept(visitor)
          }
          if (!pkg.contains("scaladsl"))
            if (hasJavadocUrl(pkg))
              javadocNode(query.javaLabel(pkg), query.javaFqcn(pkg), jAnchor, node).accept(visitor)
            else
              scaladocNode("java", query.javaLabel(pkg), query.scalaFqcn(pkg) + scalaClassSuffix, jAnchor, node)
                .accept(visitor)
        }
      case n =>
        ctx.error(
          s"$n matches found for $query, but not javadsl/scaladsl: ${matches.mkString(", ")}. " +
              s"You may want to use the fully qualified class name as @apidoc[fqcn] instead of @apidoc[$query]. " +
              s"For examples see https://github.com/lightbend/sbt-paradox-apidoc#examples",
          node
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
