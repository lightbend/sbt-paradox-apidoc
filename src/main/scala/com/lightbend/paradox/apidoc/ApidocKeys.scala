/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.paradox.apidoc

import sbt._

object ApidocKeys {
  val apidocRootPackage = settingKey[String]("")
}
