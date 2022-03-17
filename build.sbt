import scala.collection.JavaConverters._

scalaVersion := "2.12.15"

sbtPlugin        := true
crossSbtVersions := List("1.0.0")
organization     := "com.lightbend.paradox"
name             := "sbt-paradox-apidoc"

addSbtPlugin(Library.sbtParadox)
libraryDependencies ++= Seq(
  Library.classgraph,
  Library.scalatest      % Test,
  Library.paradoxTestkit % Test
)

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/lightbend/sbt-paradox-apidoc"))
scmInfo := Some(
  ScmInfo(url("https://github.com/lightbend/sbt-paradox-apidoc"), "git@github.com:lightbend/sbt-paradox-apidoc.git")
)
developers += Developer(
  "contributors",
  "Contributors",
  "https://gitter.im/lightbend/paradox",
  url("https://github.com/lightbend/sbt-paradox-apidoc/graphs/contributors")
)
organizationName     := "Lightbend Inc."
organizationHomepage := Some(url("https://lightbend.com"))
startYear            := Some(2018)

enablePlugins(AutomateHeaderPlugin)
scalafmtOnCompile := true

enablePlugins(SbtPlugin)
scriptedLaunchOpts += ("-Dproject.version=" + version.value)
scriptedLaunchOpts ++= java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.filter(a =>
  Seq("-Xmx", "-Xms", "-XX", "-Dfile").exists(a.startsWith)
)

packageSrc / publishArtifact := false

// Disable publish for now
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

ThisBuild / githubWorkflowJavaVersions := List(
  JavaSpec.temurin("8"),
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17")
)

ThisBuild / githubWorkflowTargetBranches := Seq("master")
