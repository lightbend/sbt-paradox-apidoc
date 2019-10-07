import sbt._

object Version {
  val classgraph = "4.8.47"
  val jtidy      = "r938"
  val sbtParadox = "0.6.5+13-57ce4875+20191007-1113"
  val scalatest  = "3.0.8"
}

object Library {
  val classgraph = "io.github.classgraph"  % "classgraph"  % Version.classgraph
  val jtidy      = "net.sf.jtidy"          % "jtidy"       % Version.jtidy
  val sbtParadox = "com.lightbend.paradox" % "sbt-paradox" % Version.sbtParadox
  val scalatest  = "org.scalatest"         %% "scalatest"  % Version.scalatest
}
