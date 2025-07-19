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
import nz.net.ultraq.gradle.fluent.DefaultMavenPublicationBuilderBuilder
import nz.net.ultraq.gradle.fluent.DefaultZipDistributionBuilder
import nz.net.ultraq.gradle.fluent.GroovyApplicationProjectBuilder
import nz.net.ultraq.gradle.fluent.GroovyGradlePluginProjectBuilder
import nz.net.ultraq.gradle.fluent.GroovyProjectBuilder
import nz.net.ultraq.gradle.fluent.MavenPublicationBuilder
import nz.net.ultraq.gradle.fluent.ZipDistributionBuilder

import org.gradle.api.Action
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
	 * Starts a fluent chain for working on an existing Groovy project.
	 */
	GroovyProjectBuilder asGroovyProject() {

		return new DefaultGroovyProjectBuilder(project)
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code application} plugin.
	 */
	GroovyApplicationProjectBuilder createGroovyApplicationProject(Action<? extends JavaApplication> configure) {

		return new DefaultGroovyApplicationProjectBuilder(project, configure)
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code groovy-gradle-plugin}
	 * plugin.
	 */
	GroovyGradlePluginProjectBuilder createGroovyGradlePluginProject() {

		return new DefaultGroovyGradlePluginProjectBuilder(project)
	}

	/**
	 * Extends {@link #createGroovyProject} with the {@code java-library}
	 * plugin.
	 */
	GroovyProjectBuilder createGroovyLibraryProject() {

		return new DefaultGroovyLibraryProjectBuilder(project)
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

		return new DefaultGroovyProjectBuilder(project)
	}

	/**
	 * Starts a fluent chain for configuring publishing artifacts to a Maven
	 * repository.  This will apply the {@code maven-publish} plugin and create
	 * a {@code main} publication, which all of the methods in this chain will
	 * operate on.
	 */
	MavenPublicationBuilder createMavenPublication() {

		return new DefaultMavenPublicationBuilderBuilder(project)
	}

	/**
	 * Starts a fluent chain for configuring a ZIP archive.  Applies the
	 * {@code distribution} plugin and defaults to including the main JAR, then
	 * any {@code CHANGELOG}, {@code LICENSE}, and {@code README} files in the
	 * project directory.
	 */
	ZipDistributionBuilder createZipDistribution() {

		return new DefaultZipDistributionBuilder(project)
	}
}
