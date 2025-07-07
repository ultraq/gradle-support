
Gradle Support
==============

[![Build Status](https://github.com/ultraq/gradle-support/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/gradle-support/actions)

Gradle plugins that help support my workflow.

Before this was a binary plugin repository, it was a collection of plain Gradle
build scripts that I referenced with raw GitHub URLs to tagged versions.  Gradle
has been discouraging that approach for a *very* long time, so now I'm trying to
go legit 😅  If you're looking for one of those older scripts, use the
branch/tag navigation control to select tags not ending with a `-binary` in the
name to locate them.


Installation
------------

These plugins are built targeting Gradle 9, so require Java 17 as well.

Add the Maven Central repository to the `pluginManagement` section of your
`settings.gradle` file so that the plugins can be found (the plugins are being
submitted to the Gradle Plugin repository and hopefully get approval so this
step can be removed):

```groovy
// settings.gradle
pluginManagement {
  repositories {
    mavenCentral()
  }
}
```

Then, add one of the plugins below to their respective `plugins` block.


Plugins
-------

### use-maven-central-repositories

Adds the Maven Central and Maven Central Snapshots repositories to a single
project, or all projects if added to a settings file.

```groovy
// settings.gradle or build.gradle
plugins {
  id 'nz.net.ultraq.gradle.use-maven-central-repositories' version '0.1.0'
}
```

### fluent-configuration

Adds a `configure` script block to a `build.gradle` file, within which you can
configure the project:

```groovy
// build.gradle
plugins {
  id 'nz.net.ultraq.gradle.fluent-configuration' version '0.1.0'
}

configure {
  createGroovyProject()
    .useJavaVersion(17)
    .useMavenCentralAndSnapshots()
    .configureSource()
      .withSourceDirectory('source')
      .withDependencies() {
        implementation 'org.apache.groovy:groovy:4.0.27'
      }
      .expandExtensionModuleVersion('moduleVersion', version)
    .configureTesting()
      .withTestDirectory('test')
      .withTestDependencies() {
        testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
      }
      .useJunitJupiter()
      .useJacoco()
}
```

 - `createGroovyProject`  
   Starts a fluent chain for configuring a Groovy project.  This will apply the
   `groovy` plugin, and configure the `groovydoc` task to generate docs with
   links to any Groovy SDK libraries (those starting with `groovy.` or
   `org.apache.groovy.`).

    - `useJavaVersion(int version)`  
      Sets the version of Java to use in the toolchain configuration.  This will
      also update the `groovydoc` task to generate docs with links to the Java
			SDK for Java libraries (anything starting with `java.` or `javax.`).

    - `useMavenCentralAndSnapshots`  
      Adds the Maven Central and Maven Central Snapshots repositories to the
      project by applying the [`use-maven-central-repositories`](#use-maven-central-repositories)
      plugin.

    - `configureSource`  
      Start configuration of source code -related things.

       - `withSourceDirectory(Object path)`  
         Set a combined source & resources directory to use.  This is for those
         who prefer co-locating source code and assets.

       - `withDependencies(Closure closure)`  
         Configure the dependencies for the project.

       - `expandExtensionModuleVersion(String propertyName = 'moduleVersion', String value = project.version)`  
         Expands the given property reference in the Groovy extension module
         manifest file to the given value.

    - `configureTesting`  
      Start configuration of test-related things.

       - `withTestDirectory(Object path)`  
         Set the directory in which test code and assets will reside.

       - `withTestDependencies(Closure closure)`  
         Configure the testing dependencies for the project.

    - `useJunitJupiter`  
      Configure all test suites to use JUnit Jupiter.

    - `useJacoco`
      Adds the `jacoco` plugin, making the added `jacocoTestReport` task run
			after and depend on the `test` task.  XML reports are also enabled so
			coverage data can be uploaded to services like [codecov](https://codecov.io/).
