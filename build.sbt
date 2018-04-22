scalaVersion := "2.12.4"

lazy val root = project
  .in(file("."))
  .aggregate(unidocPlugin)

lazy val unidocPlugin = project
  .in(file("plugin"))
  .settings(
    sbtPlugin := true,
    organization := "com.lightbend.akka",
    name := "sbt-paradox-unidoc",
    addSbtPlugin(Library.sbtParadox),
    libraryDependencies ++= Seq(
      Library.fastClassPathScanner,
      Library.jtidy % Test,
      Library.scalatest % Test
    )
  )
