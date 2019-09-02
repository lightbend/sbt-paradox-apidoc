/*
 * Copyright 2018 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.apidoc

import io.github.classgraph.ClassGraph
import com.lightbend.paradox.markdown.Writer
import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.paradoxDirectives
import sbt.Keys.fullClasspath
import sbt._

import scala.collection.JavaConverters._

object ApidocPlugin extends AutoPlugin {
  object autoImport extends ApidocKeys
  import autoImport._

  override def requires: Plugins      = ParadoxPlugin
  override def trigger: PluginTrigger = AllRequirements

  override def projectSettings: Seq[Setting[_]] = apidocParadoxZeroSettings

  def apidocParadoxZeroSettings: Seq[Setting[_]] = Seq(
    apidocRootPackage := "scala",
    apidocClasses := Def.taskDyn {
          val classpathProjects = apidocProjects.?.value
            .map(inProjects)
            .getOrElse {
              inAggregates(LocalRootProject, includeRoot = true)
            }
          val filter = ScopeFilter(classpathProjects, inConfigurations(Compile))
          fullClasspath.all(filter).map(_.flatMap(_.files).map(_.toURI.toURL))
        }.value,
    paradoxDirectives ++= Def.taskDyn {
          val classLoader = new java.net.URLClassLoader(apidocClasses.value.toArray, this.getClass.getClassLoader)
          val scanner = new ClassGraph()
            .whitelistPackages(apidocRootPackage.value)
            .addClassLoader(classLoader)
            .scan()
          val allClasses = scanner.getAllClasses.getNames.asScala.toVector
          Def.task {
            Seq(
              { ctx: Writer.Context =>
                new ApidocDirective(allClasses, ctx.properties)
              }
            )
          }
        }.value
  )
}
