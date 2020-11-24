libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % "2.5.25"

enablePlugins(ParadoxPlugin)
paradoxTheme  := None
paradoxGroups := Map("Language" -> Seq("Java", "Scala"))
paradoxProperties ++= Map(
  "scaladoc.akka.base_url" -> "akka-scaladoc",
  "scaladoc.akka.stream.base_url" -> "akka-stream-scaladoc",
  "javadoc.akka.base_url" -> "akka-javadoc",
  "javadoc.akka.stream.base_url" -> ""
)

apidocRootPackage := "akka"
