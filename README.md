
Gradle Support
==============

[![Build Status](https://github.com/ultraq/gradle-support/actions/workflows/build.yml/badge.svg)](https://github.com/ultraq/gradle-support/actions)
[![codecov](https://codecov.io/gh/ultraq/gradle-support/graph/badge.svg?token=AhWqCXCZzC)](https://codecov.io/gh/ultraq/gradle-support)
[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/nz.net.ultraq.gradle.fluent-configuration)](https://plugins.gradle.org/u/ultraq)

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

> With Gradle 9 now released, the goal is to test it and these plugins with the
> configuration cache before achieving 1.0.0 status.

### use-maven-central-repositories

Adds the Maven Central and Snapshots repositories to a single project, or all
projects if added to a settings file.

```groovy
// settings.gradle or build.gradle
plugins {
  id 'nz.net.ultraq.gradle.use-maven-central-repositories' version '0.9.3'
}
```

### fluent-configuration

Adds a `configure` script block to a `build.gradle` file, within which you can
configure the project:

```groovy
// build.gradle
plugins {
  id 'nz.net.ultraq.gradle.fluent-configuration' version '0.9.3'
}

configure {
  // The following sections will go in this block!
}
```

#### `createGroovyProject`

Starts a fluent chain for configuring a Groovy project.  This will apply the
`groovy` plugin, and configure the `groovydoc` task to generate docs with links
to any Groovy SDK libraries (those starting with `groovy.` or
`org.apache.groovy.`).  If the `idea` plugin is present, then it'll configure
the IDE to build to the same directories as Gradle instead of the default `out`
directory 🤢

You can also use `asGroovyProject` to continue configuring an existing project
created by `createGroovyProject`, eg: started in one build script to be picked
up later like in a multi-project build.

```groovy
configure {
  createGroovyProject()
    .useJavaVersion(17)
    .useMavenCentralRepositories()
    .withJavaCompileOptions() {
      options.compilerArgs = ['-Aname="Value"']
    }
    .withGroovyCompileOptions() {
      groovyOptions.parameters = true
    }
    .withGroovydocOptions() {
      overviewText = resources.text.fromString('Hello!')
    }
    .withJarOptions() {
      manifest {
        attributes 'Automatic-Module-Name': 'org.example.myproject'
      }
    }
    .withSourcesJar()
    .withGroovydocJar()
    .configureSource()
      .withSourceDirectory('source')
      .withDependencies() {
        implementation 'org.apache.groovy:groovy:4.0.27'
      }
      .expandExtensionModuleVersion('moduleVersion', version)
      .expand('**/*.properties', [version: version])
    .configureVerification()
      .withTestDirectory('test')
      .withTestDependencies() {
        testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
      }
      .useCodenarc(resources.text.fromUri('https://example.org/path-to-codenarc-config.groovy'))
      .useJUnitJupiter()
      .useJacoco()
}
```

 - `useJavaVersion(int version)`  
    Sets the version of Java to use in the toolchain configuration.  This will
    also update the `groovydoc` task to generate docs with links to the Java
    SDK for Java libraries (anything starting with `java.` or `javax.`).

 - `useMavenCentralRepositories`  
    Adds the Maven Central and Snapshots repositories to the project by
    applying the [`use-maven-central-repositories`](#use-maven-central-repositories)
    plugin.

 - `withJavaCompileOptions(Action<? extends JavaCompile> configure)`  
   Pass any compilation options to the `compileJava` task.

 - `withGroovyCompileOptions(Action<? extends GroovyCompile> configure)`  
   Pass any compilation options to the `compileGroovy` task.

 - `withGroovydocOptions(Action<? extends Groovydoc> configure)`  
   Pass any groovydoc options to the `groovydoc` task.

 - `withJarOptions(Action<? extends Jar> configure)`  
   Configures the `jar` task.

 - `withSourcesJar(Action<? extends Jar> configure = null)`  
   Adds a sources JAR archive as output for the build.  The task can optionally
   be configured by providing a closure.

 - `withGroovydocJar()`  
   Adds a groovydoc JAR archive as output for the build.

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

 - `configureVerification`  
   Start configuration of test-related things.
 
    - `withTestDirectory(Object path)`  
      Set the directory in which test code and assets will reside.

    - `withTestDependencies(Closure closure)`  
     Configure the testing dependencies for the project.

    - `useCodenarc(TextResource codenarcConfig)`  
      Adds the `codenarc` plugin and specifies the config file to use.

    - `useJUnitJupiter`  
      Configure all test suites to use JUnit Jupiter.
 
    - `useJacoco`  
      Adds the `jacoco` plugin, making the added `jacocoTestReport` task run
      after and depend on the `test` task.  XML reports are also enabled so
      coverage data can be uploaded to services like [codecov](https://codecov.io/).

#### `createGroovyLibraryProject`

Extends `createGroovyProject` to also apply the `java-library` plugin.  Also
comes with `asGroovyLibraryProject` to continue configuring any existing Groovy
project as a library one.

#### `createGroovyApplicationProject(Action<? extends JavaApplication> configure)`

Extends `createGroovyProject` to also apply the `application` plugin.  Also
comes with `asGroovyApplicationProject` to continue configuring any existing
Groovy project as an application one.

#### `createGroovyGradlePluginProject`

Extends `createGroovyProject` to also apply the `groovy-gradle-plugin` plugin.

 - `useGradlePluginPortal`  
   Adds the Gradle Plugin Portal repository to the project.

#### `createMavenPublication`

Starts a fluent chain for configuring publishing artifacts to a Maven
repository.  This will apply the `maven-publish` plugin and create a `main`
publication which all of the methods in this chain will operate on.

```groovy
configure {
  createMavenPublication()
    .withArtifacts(groovydocJar)
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
}
```

 - `withArtifacts(Object... sources)`  
   Add more artifacts to the publication (the main JAR and sources JAR are
   already included).  If `groovydocJar` is one of the artifacts, it will be
   given a `javadoc` classifier so that it can be used as the documentation
   companion for the main JAR and so that services like [javadoc.io](https://javadoc.io)
   can display it.

 - `configurePom(MavenPom pom, Action<? extends MavenPom> configure = null)`  
   Starts a fluent chain to configure the Maven POM.  The Gradle project `name`
   and `description` properties will also be used for their respective POM
   elements.

    - `useApache20License`  
      Automatically fill in the `<licences>` section to have a license of the
      Apache 2.0 license.

    - `withGitHubScm(String user, String repo = rootProject.name)`  
      Automatically fill in the `<scm>` section to reference a GitHub project.
      The repository will default to the root project name.

    - `withDevelopers(Map<String, String>... developers)`  
      Set the `<developers>` section with the given developers.  The map
      properties accepted are `name`, `email`, and `url`.

 - `publishTo(Action<? extends MavenArtifactRepository> configure)`  
   Publish to any Maven repository of your configuration.  If looking to publish
   to Maven Central, there's a separate configuration chain,
   [`createMavenCentralPublisherBundle`](#createmavencentralpublisherbundle),
   for that just below.

#### `createMavenCentralPublisherBundle`

Starts a fluent chain for taking one or more Maven publications and submitting
them to Maven Central via their new Publisher API.  This can be done for a
single project build, or for a multi-project build with multiple artifacts all
under the same namespace.

```groovy
configure {
  createMavenCentralPublisherBundle()
    .forProjects(*subprojects)
    .useAutomaticPublishing()
    .withCredentials(
      findProperty('mavenCentralPublisherUsername'),
      findProperty('mavenCentralPublisherPassword')
    )
}
```

 - `forProjects(Project... projects)`  
   Select which projects to include in the upload bundle.  Each included project
   should have a Maven publication already configured so that this task can make
   sure the publication is signed and configured to deploy to a staging
   directory that will live at `build/staging-deploy`.

 - `forThisProject()`  
   Convenience method to use the current project as the only one for publishing.

 - `useAutomaticPublishing()`  
   Automatically push the bundle to Maven Central if it passes validation.

 - `withCredentials(String username, String password)`  
   Specify the username/password for publishing the staged artifacts to Maven
   Central.

   As this method takes credential information, DO NOT enter your actual
   credentials into your build script.  Instead, reference Gradle properties or
   environment variables.

#### `createZipDistribution`

Starts a fluent chain for configuring a ZIP archive.  Applies the `distribution`
plugin and defaults to including the main JAR, then any `CHANGELOG`, `LICENSE`,
and `README` files in the project directory.

```groovy
configure {
  createZipDistribution()
    .withDependenciesIn('libraries')
    .withSourcesIn('source')
    .withGroovydocsIn('groovydoc')
}
```

 - `withDependenciesIn(String directory)`  
   Include runtime dependencies of the main JAR and place them in the given
   directory.

 - `withSourcesIn(String directory)`  
   Include source code and place them in the given directory.

 - `withGroovydocsIn(String directory)`  
   Include Groovydocs and place them in the given directory.
