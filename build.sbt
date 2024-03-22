import scala.collection.JavaConverters._

ThisBuild / scalaVersion := "2.12.19"

sbtPlugin        := true
crossSbtVersions := List("1.4.9")
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

enablePlugins(SbtPlugin)
scriptedLaunchOpts += ("-Dproject.version=" + version.value)
scriptedLaunchOpts ++= java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.filter(a =>
  Seq("-Xmx", "-Xms", "-XX", "-Dfile").exists(a.startsWith)
)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / publishTo := sonatypePublishTo.value
sonatypeProfileName   := "com.lightbend"

ThisBuild / githubWorkflowJavaVersions := List(
  JavaSpec.temurin("11")
)

ThisBuild / githubWorkflowTargetBranches := Seq("main")
ThisBuild / githubWorkflowBuild          := Seq(WorkflowStep.Sbt(List("test", "scripted")))
