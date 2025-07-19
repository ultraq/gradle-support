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

package nz.net.ultraq.gradle

import nz.net.ultraq.gradle.fluent.GroovyApplicationProjectConfig
import nz.net.ultraq.gradle.fluent.GroovyGradlePluginProjectConfig
import nz.net.ultraq.gradle.fluent.GroovyProjectConfig
import nz.net.ultraq.gradle.fluent.MavenPublicationConfig
import nz.net.ultraq.gradle.fluent.ZipDistributionConfig

import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication

import groovy.transform.TupleConstructor

/**
 * The {@code configure} script block and the entry-point methods for the fluent
 * API.
 */
@TupleConstructor(defaults = false)
abstract class FluentConfigurationPluginExtension {

	final Project project

	/**
	 * Extends {@link #createGroovyProject} with the {@code application} plugin.
	 */
	GroovyApplicationProjectConfig createGroovyApplicationProject(@DelegatesTo(JavaApplication) Closure configure) {

		return new DefaultGroovyApplicationProjectConfig(project, configure)
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code groovy-gradle-plugin}
	 * plugin.
	 */
	GroovyGradlePluginProjectConfig createGroovyGradlePluginProject() {

		return new DefaultGroovyGradlePluginProjectConfig(project)
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code java-library}
	 * plugin.
	 */
	GroovyProjectConfig createGroovyLibraryProject() {

		return new DefaultGroovyLibraryProjectConfig(project)
	}

	/**
	 * Starts a fluent chain for configuring a Groovy project.  This will apply
	 * the {@code groovy} plugin, and configure the {@code groovydoc} task to
	 * generate docs with links to any Groovy SDK libraries (those starting with
	 * {@code groovy.} or {@code org.apache.groovy.}).  If the {@code idea} plugin
	 * is present, then it'll configure the IDE to build to the same directories
	 * as Gradle instead of the default {@code out} directory 🤢
	 */
	GroovyProjectConfig createGroovyProject() {

		return new DefaultGroovyProjectConfig(project)
	}

	/**
	 * Starts a fluent chain for configuring publishing artifacts to a Maven
	 * repository.  This will apply the {@code maven-publish} plugin and create
	 * a {@code main} publication, which all of the methods in this chain will
	 * operate on.
	 */
	MavenPublicationConfig createMavenPublication() {

		return new DefaultMavenPublicationConfig(project)
	}

	/**
	 * Starts a fluent chain for configuring a ZIP archive.  Applies the
	 * {@code distribution} plugin and defaults to including the main JAR, then
	 * any {@code CHANGELOG}, {@code LICENSE}, and {@code README} files in the
	 * project directory.
	 */
	ZipDistributionConfig createZipDistribution() {

		return new DefaultZipDistributionConfig(project)
	}
}
