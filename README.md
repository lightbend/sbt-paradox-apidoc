# sbt-paradox-apidoc [![bintray-badge][]][bintray] [![travis-badge][]][travis]

[bintray]:               https://bintray.com/sbt/sbt-plugin-releases/sbt-paradox-apidoc
[bintray-badge]:         https://api.bintray.com/packages/sbt/sbt-plugin-releases/sbt-paradox-apidoc/images/download.svg
[travis]:                https://travis-ci.com/lightbend/sbt-paradox-apidoc
[travis-badge]:          https://travis-ci.com/lightbend/sbt-paradox-apidoc.svg?branch=master

A [paradox](https://github.com/lightbend/paradox/) directive that automatically adds links for classes from documentation to scaladoc and javadoc.

## Usage

```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-apidoc" % "<latest>")
```

You can now create 'grouped' javadoc/scaladoc references in paradox like:

```
Look into the documentation for @apidoc[MyClass].
```

This will automatically find the FQCN of the class when it is unique. When the
name does not uniquely identify the class, the plugin will check for the
`scaladsl`/`javadsl` package convention found in Akka projects. If that doesn't
produce an unambigious result, you will have to use the FQCN.

## Examples

[See details in the tests](/src/test/scala/com/lightbend/paradox/apidoc/ApidocDirectiveSpec.scala)

* `@apidoc[Actor]` (Just one class exists.)
    * classes: `akka.actor.Actor`
    * Scala:  `Actor` - `akka/actor/Actor.html`
    * Java:  `Actor` - `akka/actor/Actor.html`

* `@apidoc[Flow]` (Both scaladoc and javadoc exist.)
    * classes: `akka.stream.scaladsl.Flow` - `akka.stream.javadsl.Flow`
    * Scala: Flow - `akka/stream/scaladsl/Flow.html`
    * Java: Flow -  `akka/stream/javadsl/Flow.html`

* `@apidoc[Marshaller]` (The scaladoc/javadoc split can be on different package depth.)
    * classes: `akka.http.scaladsl.marshalling.Marshaller`, `akka.http.javadsl.marshalling.Marshaller`
    * Scala: Marshaller - `akka/http/scaladsl/marshalling/Marshaller.html`
    * Java: Marshaller - `akka/http/javadsl/marshalling/Marshaller.html`

* `@apidoc[typed.*.Replicator$]` (The classes exist in multiple places.)
    * classes: `akka.cluster.ddata.Replicator$`, `akka.cluster.ddata.typed.scaladsl.Replicator$`, `akka.cluster.ddata.typed.javadsl.Replicator$`
`   * Scala: Replicator - `akka/cluster/ddata/typed/scaladsl/Replicator$.html`
    * Java: Replicator - `akka/cluster/ddata/typed/javadsl/Replicator.html`

* `@apidoc[ClusterClient$]` (Link to scala object.)
    * classes: `akka.cluster.client.ClusterClient`
    * Scala: ClusterClient - `akka/cluster/client/ClusterClient$.html`
    * Java: ClusterClient - `akka/cluster/client/ClusterClient.html`

* `@apidoc[Source[ServerSentEvent, \_]]` (Show type paramters.)
    * classes: `akka.stream.scaladsl.Source` - `akka.stream.javadsl.Source`
    * Scala: Source\[ServerSentEvent, _\] - `akka/stream/scaladsl/Source.html`
    * Java: Source\<ServerSentEvent, ?\> - `akka/stream/javadsl/Source.html`

* `@apidoc[TheClass.method](Flow)` (Different link text than the class name.)
    * classes: `akka.stream.scaladsl.Flow` - `akka.stream.javadsl.Flow`
    * Scala: TheClass.method<br>`akka/stream/scaladsl/Flow.html`
    * Java: TheClass.method<br>`akka/stream/javadsl/Flow.html`

* `@apidoc[method](Flow) { scala="#method():Unit" java="#method()" }` (Link to method anchors.)
    * classes: `akka.stream.scaladsl.Flow` - `akka.stream.javadsl.Flow`
    * Scala: method - `akka/stream/scaladsl/Flow.html#method():Unit`
    * Java: method - `akka/stream/javadsl/Flow.html#method()`


To limit the packages that are searched for classes, configure the
`apidocRootPackage` setting:

```
import com.lightbend.paradox.apidoc.ApidocPlugin.autoImport.apidocRootPackage

(...)
  apidocRootPackage := "akka",
(...)
```

### When only Scaladoc is generated

If the project does not publish Javadoc, the corresponding Javadoc base URL can be cleared and the Java links will point to the `javadsl` package in the Scaladocs.

Example:
```scala
"javadoc.akka.stream.alpakka.base_url" -> ""
```

## License

The license is Apache 2.0, see LICENSE.

## Maintanance notes

**This project is NOT supported under the Lightbend subscription.**

The project is maintained mostly by [@richardimaoka](https://github.com/richardimaoka) and [@akka-team](https://github.com/orgs/lightbend/teams/akka-team/members).

Feel free to ping above maintainers for code review or discussions. Pull requests are very welcome–thanks in advance!
