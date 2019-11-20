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

import com.lightbend.paradox.markdown.Writer

class ApidocDirectiveSpec extends MarkdownBaseSpec {
  val rootPackage = "akka"

  val allClasses = Array(
    "akka.actor.ActorRef",
    "akka.actor.typed.ActorRef",
    "akka.cluster.client.ClusterClient",
    "akka.cluster.client.ClusterClient$",
    "akka.cluster.ddata.Replicator",
    "akka.cluster.ddata.Replicator$",
    "akka.cluster.ddata.typed.scaladsl.Replicator",
    "akka.cluster.ddata.typed.scaladsl.Replicator$",
    "akka.cluster.ddata.typed.javadsl.Replicator",
    "akka.cluster.ddata.typed.javadsl.Replicator$",
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
    linkRenderer        = Writer.defaultLinks,
    verbatimSerializers = Writer.defaultVerbatims,
    serializerPlugins = Writer.defaultPlugins(
      Writer.defaultDirectives ++ Seq(
            (ctx: Writer.Context) => new ApidocDirective(allClasses, ctx.properties)
          )
    )
  )

  implicit val context = writerContextWithProperties(
    "javadoc.link_style" -> "frames",
    "scaladoc.akka.base_url" -> "https://doc.akka.io/api/akka/2.5",
    "scaladoc.akka.http.base_url" -> "https://doc.akka.io/api/akka-http/current",
    "javadoc.akka.base_url" -> "https://doc.akka.io/japi/akka/2.5",
    "javadoc.akka.http.base_url" -> "https://doc.akka.io/japi/akka-http/current"
  )

  "Apidoc directive" should "generate markdown correctly when there is only one match" in {
    markdown("@apidoc[Envelope]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/dispatch/Envelope.html" title="akka.dispatch.Envelope"><code>Envelope</code></a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/dispatch/Envelope.html" title="akka.dispatch.Envelope"><code>Envelope</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "throw an exception when there is no match" in {
    val thrown = the[IllegalStateException] thrownBy markdown("@apidoc[ThereIsNoSuchClass]")
    thrown.getMessage shouldEqual
      "No matches found for ThereIsNoSuchClass"
  }

  it should "generate markdown correctly when 2 matches found and their package names include javadsl/scaladsl" in {
    markdown("@apidoc[Flow]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html" title="akka.stream.javadsl.Flow"><code>Flow</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html" title="akka.stream.scaladsl.Flow"><code>Flow</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "allow linking to a typed class that is also present in classic" in {
    markdown("@apidoc[typed.*.Replicator$]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/cluster/ddata/typed/scaladsl/Replicator$.html" title="akka.cluster.ddata.typed.scaladsl.Replicator"><code>Replicator</code></a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/cluster/ddata/typed/javadsl/Replicator.html" title="akka.cluster.ddata.typed.javadsl.Replicator"><code>Replicator</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "allow linking to a classic class that is also present in typed" in {
    markdown("@apidoc[ddata.Replicator$]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/cluster/ddata/Replicator$.html"  title="akka.cluster.ddata.Replicator"><code>Replicator</code></a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/cluster/ddata/Replicator.html" title="akka.cluster.ddata.Replicator"><code>Replicator</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "throw an exception when two matches found but javadsl/scaladsl is not in their packages" in {
    val thrown = the[IllegalStateException] thrownBy markdown("@apidoc[ActorRef]")
    thrown.getMessage shouldEqual
      "2 matches found for ActorRef, but not javadsl/scaladsl: akka.actor.ActorRef, akka.actor.typed.ActorRef. You may want to use the fully qualified class name as @apidoc[fqcn] instead of @apidoc[ActorRef]."
  }

  it should "generate markdown correctly when fully qualified class name (fqcn) is specified as @apidoc[fqcn]" in {
    markdown("@apidoc[akka.actor.ActorRef]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/actor/ActorRef.html" title="akka.actor.ActorRef"><code>ActorRef</code></a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/actor/ActorRef.html" title="akka.actor.ActorRef"><code>ActorRef</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "find a class by partial fqcn" in {
    markdown("@apidoc[actor.typed.ActorRef]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/actor/typed/ActorRef.html" title="akka.actor.typed.ActorRef"><code>ActorRef</code></a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/actor/typed/ActorRef.html" title="akka.actor.typed.ActorRef"><code>ActorRef</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for a companion object" in {
    markdown("@apidoc[ClusterClient$]") shouldEqual
      html(
        """<p><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/cluster/client/ClusterClient$.html" title="akka.cluster.client.ClusterClient"><code>ClusterClient</code></a></span><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/cluster/client/ClusterClient.html" title="akka.cluster.client.ClusterClient"><code>ClusterClient</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for type parameter and wildcard" in {
    markdown("@apidoc[Source[ServerSentEvent, \\_]]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Source.html" title="akka.stream.javadsl.Source"><code>Source&lt;ServerSentEvent, ?&gt;</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Source.html" title="akka.stream.scaladsl.Source"><code>Source[ServerSentEvent, _]</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for type parameters with concrete names" in {
    markdown("@apidoc[Flow[Message, Message, Mat]]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html" title="akka.stream.javadsl.Flow"><code>Flow&lt;Message, Message, Mat&gt;</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html" title="akka.stream.scaladsl.Flow"><code>Flow[Message, Message, Mat]</code></a></span>
          |</p>""".stripMargin
      )
  }

  it should "generate markdown correctly for nested type parameters" in {
    markdown("@apidoc[Marshaller[Try[A], B]]") shouldEqual
      html(
        """<p><span class="group-java">
          |<a href="https://doc.akka.io/japi/akka-http/current/?akka/http/javadsl/marshalling/Marshaller.html" title="akka.http.javadsl.marshalling.Marshaller"><code>Marshaller&lt;Try&lt;A&gt;, B&gt;</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka-http/current/akka/http/scaladsl/marshalling/Marshaller.html" title="akka.http.scaladsl.marshalling.Marshaller"><code>Marshaller[Try[A], B]</code></a></span>
          |</p>""".stripMargin
      )
  }

  "Anchor attributes" should "be used" in {
    markdown("""The @apidoc[Flow] { scala="#method():Unit" java="#method()" } thingie""") shouldEqual
      html(
        """<p>The <span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html#method()" title="akka.stream.javadsl.Flow"><code>Flow</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html#method():Unit" title="akka.stream.scaladsl.Flow"><code>Flow</code></a></span>
          |thingie</p>""".stripMargin
      )
  }

  "Directive with label and source" should "use the source as class pattern" in {
    markdown("The @apidoc[TheClass.method](Flow) { .scaladoc a=1 } thingie") shouldEqual
      html(
        """<p>The <span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html" title="akka.stream.javadsl.Flow"><code>TheClass.method</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html" title="akka.stream.scaladsl.Flow"><code>TheClass.method</code></a></span>
          |thingie</p>""".stripMargin
      )
  }

  it should "adapt generics notation from the label" in {
    markdown("The @apidoc[TheClass[File].method[String]](Flow) { .scaladoc a=1 } thingie") shouldEqual
      html(
        """<p>The <span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html" title="akka.stream.javadsl.Flow"><code>TheClass&lt;File&gt;.method&lt;String&gt;</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html" title="akka.stream.scaladsl.Flow"><code>TheClass[File].method[String]</code></a></span>
          |thingie</p>""".stripMargin
      )
  }

  it should "use anchors" in {
    markdown("""The @apidoc[TheClass[File].method[String]](Flow) { scala="#method():Unit" java="#method()" } thingie""") shouldEqual
      html(
        """<p>The <span class="group-java">
          |<a href="https://doc.akka.io/japi/akka/2.5/?akka/stream/javadsl/Flow.html#method()" title="akka.stream.javadsl.Flow"><code>TheClass&lt;File&gt;.method&lt;String&gt;</code></a></span><span class="group-scala">
          |<a href="https://doc.akka.io/api/akka/2.5/akka/stream/scaladsl/Flow.html#method():Unit" title="akka.stream.scaladsl.Flow"><code>TheClass[File].method[String]</code></a></span>
          |thingie</p>""".stripMargin
      )
  }
}
