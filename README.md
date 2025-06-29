
Gradle Support
==============

[![Build Status](https://github.com/ultraq/gradle-support/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/gradle-support/actions)

Gradle plugins that help support my workflow.  The plugins are very geared
towards how I work, so might not be of much use to anybody else.  Since many of
my public projects rely on them however, I thought it best to distribute the
plugins too so others can build my projects on their own machines.

Versions prior to 5.0.0 were plain Gradle build scripts that I referenced with
raw GitHub URLs to tagged versions.  Gradle has been discouraging that approach
for a *very* long time, so now I'm trying to make these legit plugins 😅  If
you're looking for one of those older scripts, use the branch/tag navigation
control to locate them.


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
  id 'nz.net.ultraq.gradle.groovy-development' version '5.0.0'
}
```
