
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
  id 'nz.net.ultraq.gradle.use-maven-central-repositories' version '5.0.0'
}
```

### single-source-directory

Make the source and resources directories be the same directory.  This is to
help those that prefer having source code and assets co-located so you're not
jumping around the file tree looking for related items.

```groovy
// build.gradle
plugins {
  id 'nz.net.ultraq.gradle.single-source-directory' version '5.0.0'
}

sourceSets {
  main {
    withSingleSourceDirectory('source')
  }
}
```

### groovy-support

Works in tandem with Gradle's built-in `groovy` plugin to help Groovy projects
achieve configuration parity with Java projects.  Mainly, by allowing Groovy
outputs/artifacts to participate in all the usual lifecycle tasks.

```groovy
// build.gradle
plugins {
  id 'nz.net.ultraq.gradle.groovy-support' version '5.0.0'
}

groovy {
	expandExtensionModuleVersion('moduleVersion', version)
  withGroovydocJar() {
    replaceJavadoc = true
  }
}
```

This plugin adds a `groovy` script block which can be used for
configuration.

The `expandExtensionModuleVersion()` method will enable processing of the Groovy
extension module manifest file, replacing `moduleVersion` property references
(eg: `$moduleVersion` or `${moduleVersion}`) with the project version.  These
are the default values and can be omitted.

The `withGroovydocJar()` method is similar to Gradle's `withJavadocJar()` in
that it adds a `groovydocJar` task to the project, and will also ensure that
task will be run when the `assemble` lifecycle tasks is used.

An optional configuration closure can be supplied to further configure the task,
though the only option right now is `replaceJavadoc` which will make the JAR use
the `javadoc` classifier so it can stand in place of the javadoc JAR.  This is
especially useful for things that rely on the presence of the javadoc JAR for
documentation, eg: Maven Central so that 'Download documentation' options in
IDEs can work, or services like [javadoc.io](https://javadoc.io) which use the
JAR as the source for documentation.
