/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.gradle.fluent

import nz.net.ultraq.gradle.UseMavenCentralRepositoriesPlugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Groovydoc

/**
 * Configuration interface for putting together a Groovy project in Gradle.
 *
 * @author Emanuel Rabina
 */
interface GroovyProjectConfig extends SourceConfigEntry, TestingEntry {

	/**
	 * Sets the version of Java to use in the toolchain configuration.  This will
	 * also update the {@code groovydoc} task to generate docs with links to the
	 * Java SDK for Java libraries (anything starting with {@code java.} or
	 * {@code javax.}).
	 */
	GroovyProjectConfig useJavaVersion(int version)

	/**
	 * Adds the Maven Central and Snapshots repositories to the project by
	 * applying the {@link UseMavenCentralRepositoriesPlugin} plugin.
	 */
	GroovyProjectConfig useMavenCentralRepositories()

	/**
	 * Pass any compilation options to the `compileGroovy` task.
	 */
	GroovyProjectConfig withGroovyCompileOptions(@DelegatesTo(GroovyCompile) Closure configure)

	/**
	 * Adds a groovydoc JAR archive as output for the build.
	 */
	GroovyProjectConfig withGroovydocJar()

	/**
	 * Pass any groovydoc options to the {@code groovydoc} task.
	 */
	GroovyProjectConfig withGroovydocOptions(@DelegatesTo(Groovydoc) Closure configure)

	/**
	 * Configure the {@code jar} task.
	 */
	GroovyProjectConfig withJarOptions(@DelegatesTo(Jar) Closure configure)

	/**
	 * Pass any compilation options to the `compileJava` task.
	 */
	GroovyProjectConfig withJavaCompileOptions(@DelegatesTo(JavaCompile) Closure configure)

	/**
	 * Add a shadow JAR archive as output for the build.  This will also disable
	 * the module metadata task as it's incorrect when building a shadow JAR.
	 */
	GroovyProjectConfig withShadowJar(@DelegatesTo(ShadowJar) Closure configure)

	/**
	 * Adds a sources JAR archive as output for the build.
	 */
	GroovyProjectConfig withSourcesJar()
}
