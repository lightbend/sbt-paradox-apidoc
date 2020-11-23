libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.25"

enablePlugins(ParadoxPlugin)
paradoxTheme  := None
paradoxGroups := Map("Language" -> Seq("Java", "Scala"))
paradoxProperties ++= Map(
  "scaladoc.akka.base_url" -> "https://doc.akka.io/api/akka/2.5",
  "javadoc.akka.base_url" -> "https://doc.akka.io/japi/akka/2.5"
)

apidocRootPackage := "akka"
