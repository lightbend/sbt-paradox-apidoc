/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.paradox.unidoc

import _root_.io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import com.lightbend.paradox.markdown.Writer
import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.paradoxDirectives
import sbt.Keys.fullClasspath
import sbt._

import scala.collection.JavaConverters._

object UnidocPlugin extends AutoPlugin {
  import UnidocKeys._

  val version = ParadoxPlugin.readProperty("akka-paradox.properties", "akka.paradox.version")

  override def requires: Plugins = ParadoxPlugin

  override def trigger: PluginTrigger = noTrigger

  override def projectSettings: Seq[Setting[_]] = unidocSettings(Compile)

  def unidocParadoxGlobalSettings: Seq[Setting[_]] = Seq(
    unidocRootPackage := "scala",
    paradoxDirectives ++= Def.taskDyn {
      val classpath = (fullClasspath in Compile).value.files.map(_.toURI.toURL).toArray
      val classLoader = new java.net.URLClassLoader(classpath, this.getClass.getClassLoader)
      val scanner = new FastClasspathScanner(unidocRootPackage.value).addClassLoader(classLoader).scan()
      val allClasses = scanner.getNamesOfAllClasses.asScala.toVector
      Def.task { Seq(
        { _: Writer.Context ⇒ new UnidocDirective(allClasses) }
      )}
    }.value
  )

  def unidocSettings(config: Configuration): Seq[Setting[_]] = unidocParadoxGlobalSettings ++ inConfig(config)(Seq(
    // scoped settings here
  ))
}
