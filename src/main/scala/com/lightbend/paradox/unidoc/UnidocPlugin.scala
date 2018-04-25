/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.paradox.unidoc

import com.lightbend.paradox.sbt.ParadoxPlugin
import sbt._

object UnidocPlugin extends AutoPlugin {

  override def requires: Plugins = ParadoxPlugin

  override def trigger: PluginTrigger = noTrigger

  override def projectSettings: Seq[Setting[_]] = unidocSettings(Compile)

  def unidocParadoxGlobalSettings: Seq[Setting[_]] = Seq(
  )

  def unidocSettings(config: Configuration): Seq[Setting[_]] = unidocParadoxGlobalSettings ++ inConfig(config)(Seq(
    // scoped settings here
  ))
}
