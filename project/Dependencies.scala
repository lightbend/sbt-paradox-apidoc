import sbt._

object Version {
  val fastClassPathScanner = "2.21"
  val jtidy                = "r938"
  val sbtParadox           = "0.4.3"
  val scalatest            = "3.0.3"
}

object Library {
  val fastClassPathScanner = "io.github.lukehutch"   %  "fast-classpath-scanner" % Version.fastClassPathScanner
  val jtidy                = "net.sf.jtidy"          %  "jtidy"                  % Version.jtidy
  val sbtParadox           = "com.lightbend.paradox" %  "sbt-paradox"            % Version.sbtParadox
  val scalatest            = "org.scalatest"         %% "scalatest"              % Version.scalatest
}
