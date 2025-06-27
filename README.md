
Gradle Support
==============

[![Build Status](https://github.com/ultraq/gradle-support/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/gradle-support/actions)

A bunch of Gradle plugins to remove common boilerplate, plus some that apply all
of them to support my workflow.  While the boilerplate plugins are useful for a
wider audience, workflow ones are very geared towards how I work so will
unlikely be of any use to others.  However, many of my public projects rely on
these, so I've made these available so others can at least build my projects on
their own machines.

Versions prior to 5.0.0 were plain Gradle build scripts that I referenced with
raw GitHub URLs to tagged versions.  Gradle has been discouraging that approach
for a *very* long time, so now I'm trying to make these legit plugins 😅  If
you're looking for one of those older scripts, use the branch/tag navigation
control to locate them.


Installation
------------

These plugins are built targeting Gradle 9, so require Java 17 as well.

### use-maven-central-repositories

Applies the Maven Central and Maven Central Snapshots repositories to the
project configuration.

```groovy
// settings.gradle or build.gradle
plugins {
  id 'nz.net.ultraq.gradle.use-maven-central-repositories' version '5.0.0-SNAPSHOT'
}
```

### 
