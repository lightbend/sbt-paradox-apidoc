# sbt-paradox-apidoc [![bintray-badge][]][bintray] [![travis-badge][]][travis]

[bintray]:               https://bintray.com/sbt/sbt-plugin-releases/sbt-paradox-apidoc
[bintray-badge]:         https://api.bintray.com/packages/sbt/sbt-plugin-releases/sbt-paradox-apidoc/images/download.svg
[travis]:                https://travis-ci.org/lightbend/sbt-paradox-apidoc
[travis-badge]:          https://travis-ci.org/lightbend/sbt-paradox-apidoc.svg?branch=master

A [paradox](https://github.com/lightbend/paradox/) directive that automatically adds links for classes from documentation to scaladoc and javadoc.

## Usage

```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-apidoc" % "<latest>")
```

You can now create 'grouped' javadoc/scaladoc references in paradox like:

```
Look into the documentation for @apidoc[MyClass].
```

This will automatically find the FQDN of the class when it is unique. When the
name does not uniquely identify the class, the plugin will check for the
`scaladsl`/`javadsl` package convention found in Akka projects. If that doesn't
produce an unambigious result, you will have to use the FQDN.

To limit the packages that are searched for classes, configure the
`apidocRootPackage` setting:

```
import com.lightbend.paradox.apidoc.ApidocPlugin.autoImport.apidocRootPackage

(...)
  apidocRootPackage := "akka",
(...)
```

## License

The license is Apache 2.0, see LICENSE.

## Maintanance notes

**This project is NOT supported under the Lightbend subscription.**

The project is maintained mostly by [@richardimaoka](https://github.com/richardimaoka) and [@akka-team](https://github.com/orgs/lightbend/teams/akka-team/members).

Feel free to ping above maintainers for code review or discussions. Pull requests are very welcome–thanks in advance!
