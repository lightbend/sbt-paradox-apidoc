/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.paradox.unidoc

import com.lightbend.paradox.markdown.Writer

class UnidocPluginSpec extends MarkdownBaseSpec {
  val rootPackage = "akka"

  val allClasses = Array(
    "akka.actor.ActorRef",
    "akka.actor.typed.ActorRef",
    "akka.cluster.client.ClusterClient",
    "akka.cluster.client.ClusterClient$",
    "akka.dispatch.Envelope",
    "akka.http.javadsl.model.sse.ServerSentEvent",
    "akka.http.javadsl.marshalling.Marshaller",
    "akka.http.javadsl.marshalling.Marshaller$",
    "akka.http.scaladsl.marshalling.Marshaller",
    "akka.http.scaladsl.marshalling.Marshaller$",
    "akka.stream.javadsl.Source",
    "akka.stream.javadsl.Source$",
    "akka.stream.scaladsl.Source",
    "akka.stream.scaladsl.Source$",
    "akka.stream.javadsl.Flow",
    "akka.stream.javadsl.Flow$",
    "akka.stream.scaladsl.Flow",
    "akka.stream.scaladsl.Flow$"
  )

  override val markdownWriter = new Writer(
    linkRenderer = Writer.defaultLinks,
    verbatimSerializers = Writer.defaultVerbatims,
    serializerPlugins = Writer.defaultPlugins(
      Writer.defaultDirectives ++ Seq(
        (_: Writer.Context) => new UnidocDirective(allClasses)
      )
    )
  )

  implicit val context = writerContextWithProperties(
    "scaladoc.akka.base_url" -> "https://doc.akka.io/api/akka/2.5",
    "scaladoc.akka.http.base_url" -> "https://doc.akka.io/api/akka-http/current",
    "javadoc.akka.base_url" -> "https://doc.akka.io/japi/akka/2.5",
    "javadoc.akka.http.base_url" -> "https://doc.akka.io/japi/akka-http/current",
  )

  "Unidoc directive" should "generate markdown correctly when there is only one match" in {
    markdown("@unidoc[Envelope]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/dispatch/Envelope.html">Envelope</a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/dispatch/Envelope.html">Envelope</a></span>
          |</p>""".stripMargin
      )
  }

  it should "throw an exception when there is no match" in {
    val thrown = the[IllegalStateException] thrownBy markdown("@unidoc[ThereIsNoSuchClass]")
    thrown.getMessage shouldEqual
      "No matches found for ThereIsNoSuchClass"
  }


  it should "generate markdown correctly when 2 matches found and their package names include javadsl/scaladsl" in {
    markdown("@unidoc[Flow]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html">Flow</a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html">Flow</a></span>
          |</p>""".stripMargin
      )
  }

  it should "throw an exception when two matches found but javadsl/scaladsl is not in their packages" in {
    val thrown = the[IllegalStateException] thrownBy markdown("@unidoc[ActorRef]")
    thrown.getMessage shouldEqual
      "2 matches found for ActorRef, but not javadsl/scaladsl: akka.actor.ActorRef, akka.actor.typed.ActorRef. You may want to use the fully qualified class name as @unidoc[fqcn] instead of @unidoc[ActorRef]."
  }

  it should "generate markdown correctly when fully qualified class name (fqcn) is specified as @unidoc[fqcn]" in {
    markdown("@unidoc[akka.actor.ActorRef]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/actor/ActorRef.html">ActorRef</a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/actor/ActorRef.html">ActorRef</a></span>
          |</p>""".stripMargin
      )
  }

  it should "throw an exception when `.` is in the [label], but the label is not fqcn" in {
    val thrown = the[IllegalStateException] thrownBy markdown("@unidoc[actor.typed.ActorRef]")
    thrown.getMessage shouldEqual
      "fqcn not found by @unidoc[actor.typed.ActorRef]"
  }

  it should "generate markdown correctly for a companion object" in {
    markdown("@unidoc[ClusterClient$]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/cluster/client/ClusterClient$.html">ClusterClient</a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/cluster/client/ClusterClient$.html">ClusterClient</a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for type parameter and wildcard" in {
    markdown("@unidoc[Source[ServerSentEvent, \\_]]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Source.html">Source&lt;ServerSentEvent, ?&gt;</a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Source.html">Source[ServerSentEvent, _]</a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for type parameters with concrete names" in {
    markdown("@unidoc[Flow[Message, Message, Mat]]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html">Flow&lt;Message, Message, Mat&gt;</a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html">Flow[Message, Message, Mat]</a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for nested type parameters" in {
    markdown("@unidoc[Marshaller[Try[A], B]]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka-http/current/?akka/http/javadsl/marshalling/Marshaller.html">Marshaller&lt;Try&lt;A&gt;, B&gt;</a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka-http/current/akka/http/scaladsl/marshalling/Marshaller.html">Marshaller[Try[A], B]</a></span>
          |</p>""".stripMargin
      )
  }
}
