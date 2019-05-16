import sbt._

object Version {
  val classgraph = "4.8.36"
  val jtidy      = "r938"
  val sbtParadox = "0.4.3"
  val scalatest  = "3.0.7"
}

object Library {
  val classgraph = "io.github.classgraph"  % "classgraph"  % Version.classgraph
  val jtidy      = "net.sf.jtidy"          % "jtidy"       % Version.jtidy
  val sbtParadox = "com.lightbend.paradox" % "sbt-paradox" % Version.sbtParadox
  val scalatest  = "org.scalatest"         %% "scalatest"  % Version.scalatest
}
