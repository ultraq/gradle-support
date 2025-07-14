
Gradle Support
==============

[![Build Status](https://github.com/ultraq/gradle-support/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/gradle-support/actions)
[![codecov](https://codecov.io/gh/ultraq/gradle-support/graph/badge.svg?token=AhWqCXCZzC)](https://codecov.io/gh/ultraq/gradle-support)
![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/nz.net.ultraq.gradle.fluent-configuration)

Gradle plugins that help support my workflow.

Before this was a binary plugin repository, it was a collection of plain Gradle
build scripts that I referenced with raw GitHub URLs to tagged versions.  Gradle
has been discouraging that approach for a *very* long time, so now I'm trying to
go legit 😅  If you're looking for one of those older scripts, use the
branch/tag navigation control to select tags not ending with `-binary` to locate
them.


Plugins
-------

These plugins are built targeting Gradle 9, so require Java 17 as well, and are
available on the Gradle Plugin Portal, so only need to be added by their ID and
version to their respective `plugins` block.

### use-maven-central-repositories

Adds the Maven Central and Snapshots repositories to a single project, or all
projects if added to a settings file.

```groovy
// settings.gradle or build.gradle
plugins {
  id 'nz.net.ultraq.gradle.use-maven-central-repositories' version '0.2.2'
}
```

### fluent-configuration

Adds a `configure` script block to a `build.gradle` file, within which you can
configure the project:

```groovy
// build.gradle
plugins {
  id 'nz.net.ultraq.gradle.fluent-configuration' version '0.2.2'
}

configure {
  createGroovyLibrary()
    .useJavaVersion(17)
    .useMavenCentralRepositories()
    .withCompileOptions() {
      groovyOptions.parameters = true
    }
    .withGroovydocOptions {
      overviewText = resources.text.fromString('Hello!')
    }
    .configureSource()
      .withSourceDirectory('source')
      .withDependencies() {
        implementation 'org.apache.groovy:groovy:4.0.27'
      }
      .expandExtensionModuleVersion('moduleVersion', version)
      .expand('**/*.properties', [version: version])
    .configureTesting()
      .withTestDirectory('test')
      .withTestDependencies() {
        testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
      }
      .useJUnitJupiter()
      .useJacoco()

  createMavenPublication()
    .addJar() {
      manifest {
        attributes 'Automatic-Module-Name': 'org.example.myproject'
      }
    }
    .addSourcesJar()
    .addGroovydocJar()
    .configurePom() {
      inceptionYear = '2025'
    }
      .useApache20License()
      .withGitHubScm('github-user', 'github-repository')
      .withDevelopers([
        name: 'My Name',
        email: 'me@example.org',
        url: 'https://example.org'
      ])
    .publishToMavenCentral(
      property('mavenCentralPublisherUsername'),
      property('mavenCentralPublisherPassword')
    )
}
```

#### `createGroovyProject`

Starts a fluent chain for configuring a Groovy project.  This will apply the
`groovy` plugin, and configure the `groovydoc` task to generate docs with links
to any Groovy SDK libraries (those starting with `groovy.` or
`org.apache.groovy.`).

 - `useJavaVersion(int version)`  
    Sets the version of Java to use in the toolchain configuration.  This will
    also update the `groovydoc` task to generate docs with links to the Java
    SDK for Java libraries (anything starting with `java.` or `javax.`).

 - `useMavenCentralRepositories`  
    Adds the Maven Central and Snapshots repositories to the project by
    applying the [`use-maven-central-repositories`](#use-maven-central-repositories)
    plugin.

 - `withCompileOptions(@DelegatesTo(GroovyCompile) Closure configure)`  
   Pass any compilation options to the `compileGroovy` task.

 - `withGroovydocOptions(@DelegatesTo(Groovydoc) Closure configure)`  
   Pass any groovydoc options to the `groovydoc` task.

 - `configureSource`  
   Start configuration of source code -related things.

    - `withSourceDirectory(Object path)`  
      Set a combined source & resources directory to use.  This is for those who
      prefer co-locating source code and assets.

    - `withDependencies(Closure closure)`  
      Configure the dependencies for the project.

    - `expandExtensionModuleVersion(String propertyName = 'moduleVersion', String value = project.version)`  
      Expands the given property reference in the Groovy extension module
      manifest file to the given value.

    - `expand(String filePattern, Map<String, String> replacements)`  
      Expand any of the keys in `replacements` to their mapped values, for any
      file matched by `filePattern`.

 - `configureTesting`  
   Start configuration of test-related things.
 
    - `withTestDirectory(Object path)`  
      Set the directory in which test code and assets will reside.

    - `withTestDependencies(Closure closure)`  
     Configure the testing dependencies for the project.

    - `useJUnitJupiter`  
      Configure all test suites to use JUnit Jupiter.
 
    - `useJacoco`  
      Adds the `jacoco` plugin, making the added `jacocoTestReport` task run
      after and depend on the `test` task.  XML reports are also enabled so
      coverage data can be uploaded to services like [codecov](https://codecov.io/).

#### `createGroovyLibrary`

Extends `createGroovyProject` to also apply the `groovy-library` plugin.

#### `createMavenPublication`

Starts a fluent chain for configuring publishing artifacts to a Maven
repository.  This will apply the `maven-publish` plugin and create a `main`
publication which all of the methods in this chain will operate on.

 - `addJar(Closure configure = null)`  
   Adds the main software component to the bundle which can be optionally
   configured with the given closure.

 - `addSourcesJar`  
   Adds the `sourcesJar` task and makes it part of the bundle to publish.

 - `addGroovydocJar`  
   Adds a `groovydocJar` task, making it part of the bundle to publish.  It will
   have a `javadoc` classifier so that it can be used as the documentation
   companion for the compiled code, and so that services like [javadoc.io](https://javadoc.io)
   can find it.  The task will also have a  dependency on the `assemble`
   lifecycle task so it can be created alongside other artifact outputs.

 - `configurePom(Closure configure)`  
   Configure the POM that will get published.  The Gradle project `name` and
   `description` properties will also be used for their respective POM elements.

    - `useApache20License`  
      Automatically fill in the `<licences>` section to have a license of the
      Apache 2.0 license.

    - `withGitHubScm(String user, String repo = project.name)`  
      Automatically fill in the `<scm>` section to reference a GitHub project.
      The repository will default to the project name.

    - `withDevelopers(Map<String,String>... developers)`  
      Set the `<developers>` section with the given developers.  The map
      properties accepted are `name`, `email`, and `url`.

 - `publishToMavenCentral(String username, String password)`  
   Configure Maven Central publishing.  This will set up both the Maven Central
   and Snapshot repositories (pushing to snapshots if the project version ends
   with `-SNAPSHOT`), and apply the `signing` plugin.

   Note that this is currently using the transitional Portal OSSRH Staging API
   that Sonatype has created to allow people to slowly migrate to their newer
   Publisher API.  This will be rewritten to utilize the Publisher API in
   future.
 
   As this method takes credential information, DO NOT enter your actual
   credentials into your build script.  Instead, reference Gradle properties or
   environment variables.

 - `publishTo(@DelegatesTo(MavenArtifactRepository) Closure configure)`  
   Publish to any Maven repository of your configuration.
