# Releasing

Releasing from CI was removed as Bintray passed away.

1. Wait until all running [Travis CI jobs](https://travis-ci.org/lightbend/sbt-paradox-apidoc/builds) complete, if any.
1. Fix up the [draft release](https://github.com/lightbend/sbt-paradox-apidoc/releases) created by the release drafter and set the next tag version (e.g. `v0.2`)
1. Ensure you're on JDK 8
1. Pull the tag, build and publish the release to Sonatype
