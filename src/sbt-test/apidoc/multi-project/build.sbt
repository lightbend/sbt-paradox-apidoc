val lib = project

val docs = project
  .enablePlugins(ParadoxPlugin)
  .settings(
    paradoxTheme := None,
    paradoxGroups := Map("Language" -> Seq("Java", "Scala")),
    paradoxProperties ++= Map(
      "scaladoc.apidoc.base_url" -> "https://localhost:8000",
      "javadoc.apidoc.base_url" -> "https://localhost:8000"
    ),
    apidocRootPackage := ""
  )

val root = project
  .in(file("."))
  .aggregate(lib)
