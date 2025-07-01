
Gradle Support
==============

[![Build Status](https://github.com/ultraq/gradle-support/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/gradle-support/actions)

Gradle plugins that help support my workflow.  The plugins are very geared
towards how I work, so might not be of much use to anybody else.  Since many of
my public projects rely on them however, I thought it best to distribute these
plugins so others can build my projects on their own machines.

Before this was a binary plugin repository, it was a collection of plain Gradle
build scripts that I referenced with raw GitHub URLs to tagged versions.  Gradle
has been discouraging that approach for a *very* long time, so now I'm trying to
go legit 😅  If you're looking for one of those older scripts, use the
branch/tag navigation control to select tags not ending with a `-binary` in the
name to locate them.


Installation
------------

These plugins are built targeting Gradle 9, so require Java 17 as well.

```groovy
// settings.gradle
pluginManagement {
  repositories {
    mavenCentral()
  }
}

// build.gradle
plugins {
  id 'nz.net.ultraq.gradle.fluent-configuration' version '0.1.0-SNAPSHOT'
}
```


API
---

With the fluent configuration plugin added, a `configure` script block is made
available, within which you can configure the project.

```groovy
configure {
  groovyProject()
    .useJavaVersion(17)
    .sourceSets()
      .withMainSourceDirectory('source')
      .withTestSourceDirectory('test')
    .repositories()
      .useMavenCentralAndSnapshots()
    .testing()
      .useJUnitJupiter()
}
```
