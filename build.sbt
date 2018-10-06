scalaVersion := "2.12.7"

sbtPlugin := true
crossSbtVersions := List("1.0.0")
organization := "com.lightbend.paradox"
name := "sbt-paradox-apidoc"

addSbtPlugin(Library.sbtParadox)
libraryDependencies ++= Seq(
  Library.fastClassPathScanner,
  Library.jtidy % Test,
  Library.scalatest % Test
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/lightbend/sbt-paradox-apidoc"))
scmInfo := Some(ScmInfo(url("https://github.com/lightbend/sbt-paradox-apidoc"), "git@github.com:lightbend/sbt-paradox-apidoc.git"))
developers += Developer("contributors",
  "Contributors",
  "https://gitter.im/lightbend/paradox",
  url("https://github.com/lightbend/sbt-paradox-apidoc/graphs/contributors"))
organizationName := "Lightbend Inc."
startYear := Some(2018)

bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"

enablePlugins(AutomateHeaderPlugin)
