/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.paradox.apidoc

import _root_.io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import com.lightbend.paradox.markdown.Writer
import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.paradoxDirectives
import sbt.Keys.fullClasspath
import sbt._

import scala.collection.JavaConverters._

object ApidocPlugin extends AutoPlugin {
  import ApidocKeys._

  val version = ParadoxPlugin.readProperty("akka-paradox.properties", "akka.paradox.version")

  override def requires: Plugins = ParadoxPlugin

  override def trigger: PluginTrigger = AllRequirements

  override def projectSettings: Seq[Setting[_]] = apidocSettings(Compile)

  def apidocParadoxGlobalSettings: Seq[Setting[_]] = Seq(
    apidocRootPackage := "scala",
    paradoxDirectives ++= Def.taskDyn {
      val classpath = (fullClasspath in Compile).value.files.map(_.toURI.toURL).toArray
      val classLoader = new java.net.URLClassLoader(classpath, this.getClass.getClassLoader)
      val scanner = new FastClasspathScanner(apidocRootPackage.value).addClassLoader(classLoader).scan()
      val allClasses = scanner.getNamesOfAllClasses.asScala.toVector
      Def.task { Seq(
        { _: Writer.Context ⇒ new ApidocDirective(allClasses) }
      )}
    }.value
  )

  def apidocSettings(config: Configuration): Seq[Setting[_]] = apidocParadoxGlobalSettings ++ inConfig(config)(Seq(
    // scoped settings here
  ))
}
