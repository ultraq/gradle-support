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

import nz.net.ultraq.gradle.fluent.DefaultGroovyApplicationProjectBuilder
import nz.net.ultraq.gradle.fluent.DefaultGroovyGradlePluginProjectBuilder
import nz.net.ultraq.gradle.fluent.DefaultGroovyLibraryProjectBuilder
import nz.net.ultraq.gradle.fluent.DefaultGroovyProjectBuilder
import nz.net.ultraq.gradle.fluent.DefaultMavenPublicationBuilder
import nz.net.ultraq.gradle.fluent.DefaultZipDistributionBuilder
import nz.net.ultraq.gradle.fluent.GroovyApplicationProjectBuilder
import nz.net.ultraq.gradle.fluent.GroovyGradlePluginProjectBuilder
import nz.net.ultraq.gradle.fluent.GroovyLibraryProjectBuilder
import nz.net.ultraq.gradle.fluent.GroovyProjectBuilder
import nz.net.ultraq.gradle.fluent.MavenPublicationBuilder
import nz.net.ultraq.gradle.fluent.ZipDistributionBuilder

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

/**
 * The {@code configure} script block and the entry-point methods for the fluent
 * API.
 */
@CompileStatic
@TupleConstructor(defaults = false)
abstract class FluentConfigurationPluginExtension {

	final Project project

	private GroovyApplicationProjectBuilder groovyApplicationProjectBuilder
	private GroovyGradlePluginProjectBuilder groovyGradlePluginProjectBuilder
	private GroovyProjectBuilder groovyProjectBuilder
	private GroovyLibraryProjectBuilder groovyLibraryProjectBuilder

	/**
	 * Starts a fluent chain for working on an existing Groovy application project.
	 */
	GroovyApplicationProjectBuilder asGroovyApplicationProject() {

		if (!groovyApplicationProjectBuilder) {
			throw new IllegalStateException('No existing Groovy application project found - create one first with createGroovyApplicationProject()')
		}
		return groovyApplicationProjectBuilder
	}

	/**
	 * Starts a fluent chain for working on an existing Groovy Gradle plugin
	 * project.
	 */
	GroovyGradlePluginProjectBuilder asGroovyGradlePluginProject() {

		if (!groovyGradlePluginProjectBuilder) {
			throw new IllegalStateException('No existing Groovy Gradle plugin project found - create one first with createGroovyGradlePluginProject()')
		}
		return groovyGradlePluginProjectBuilder
	}

	/**
	 * Starts a fluent chain for working on an existing Groovy library project.
	 */
	GroovyLibraryProjectBuilder asGroovyLibraryProject() {

		if (!groovyLibraryProjectBuilder) {
			throw new IllegalStateException('No existing Groovy library project found - create one first with createGroovyLibraryProject()')
		}
		return groovyLibraryProjectBuilder
	}

	/**
	 * Starts a fluent chain for working on an existing Groovy project.
	 */
	GroovyProjectBuilder asGroovyProject() {

		if (!groovyProjectBuilder) {
			throw new IllegalStateException('No existing Groovy project found - create one first with createGroovyProject()')
		}
		return groovyProjectBuilder
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code application} plugin.
	 */
	GroovyApplicationProjectBuilder createGroovyApplicationProject(Action<? extends JavaApplication> configure) {

		groovyApplicationProjectBuilder = project.objects.newInstance(DefaultGroovyApplicationProjectBuilder, project, configure)
		return groovyApplicationProjectBuilder
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code groovy-gradle-plugin}
	 * plugin.
	 */
	GroovyGradlePluginProjectBuilder createGroovyGradlePluginProject() {

		groovyGradlePluginProjectBuilder = project.objects.newInstance(DefaultGroovyGradlePluginProjectBuilder, project)
		return groovyGradlePluginProjectBuilder
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code java-library}
	 * plugin.
	 */
	GroovyProjectBuilder createGroovyLibraryProject() {

		groovyLibraryProjectBuilder = project.objects.newInstance(DefaultGroovyLibraryProjectBuilder, project)
		return groovyLibraryProjectBuilder
	}

	/**
	 * Starts a fluent chain for configuring a Groovy project.  This will apply
	 * the {@code groovy} plugin, and configure the {@code groovydoc} task to
	 * generate docs with links to any Groovy SDK libraries (those starting with
	 * {@code groovy.} or {@code org.apache.groovy.}).  If the {@code idea} plugin
	 * is present, then it'll configure the IDE to build to the same directories
	 * as Gradle instead of the default {@code out} directory 🤢
	 */
	GroovyProjectBuilder createGroovyProject() {

		groovyProjectBuilder = project.objects.newInstance(DefaultGroovyProjectBuilder, project)
		return groovyProjectBuilder
	}

	/**
	 * Starts a fluent chain for configuring publishing artifacts to a Maven
	 * repository.  This will apply the {@code maven-publish} plugin and create
	 * a {@code main} publication, which all of the methods in this chain will
	 * operate on.
	 */
	MavenPublicationBuilder createMavenPublication() {

		return project.objects.newInstance(DefaultMavenPublicationBuilder, project)
	}

	/**
	 * Starts a fluent chain for configuring a ZIP archive.  Applies the
	 * {@code distribution} plugin and defaults to including the main JAR, then
	 * any {@code CHANGELOG}, {@code LICENSE}, and {@code README} files in the
	 * project directory.
	 */
	ZipDistributionBuilder createZipDistribution() {

		return project.objects.newInstance(DefaultZipDistributionBuilder, project)
	}
}
