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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * <p>Gradle plugin for my JVM-based projects.</p>
 *
 * <p>This plugin will apply several conventions, based on the plugins used in
 * the project.  It will always:</p>
 * <ul>
 *   <li>Add {@code MavenCentral} and {@code MavenSnapshot} repositories</li>
 * </ul>
 *
 * <p>If the {@code java} plugin is present, it will:</p>
 * <ul>
 *   <li>Configure {@code source} as the only Java source and resources
 *     directory</li>
 *   <li>Configure {@code test} as the only Java test source and resources
 *     directory</li>
 * </ul>
 *
 * <p>If the {@code groovy} plugin is present, it will:</p>
 * <ul>
 *   <li>Configure {@code source} as the only Groovy source and resources
 *     directory</li>
 *   <li>Configure {@code test} as the only Groovy test source and resources
 *     directory</li>
 * </ul>
 *
 * <p>If the {@code codenarc} plugin is present, it will:</p>
 * <ul>
 *   <li>Load my codenarc config from the
 *     <a href="https://github.com/ultraq/codenarc-config-ultraq">codenarc-config-ultraq</a>
 *     repository</li>
 * </ul>
 *
 * <p>If the {@code jacoco} plugin is present, it will:</p>
 * <ul>
 *   <li>Configure the report output so that it can be used with
 *     <a href="https://codecov.io/">codecov</a></li>
 * </ul>
 *
 * <p>If the {@code idea} plugin is present, it will:</p>
 * <ul>
 *   <li>Match the output directories to be the same as the Gradle ones</li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
class GroovyDevelopmentPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		configureRepositories(project)
		configureDirectories(project)
		configureResources(project)
		configureGroovydocs(project)
		configureVerification(project)
		configureDistribution(project)
	}

	/**
	 * Set {@code source} and {@code test} as the combined source & resource
	 * directories for their respective sourcesets.
	 */
	private void configureDirectories(Project project) {

		project.pluginManager.withPlugin('groovy') {
			project.extensions.getByType(SourceSetContainer).configureEach { sourceSet ->
				[sourceSet.java, sourceSet.extensions.getByType(GroovySourceDirectorySet), sourceSet.resources]*.srcDirs =
					[project.file(sourceSet.name == 'main' ? 'source' : 'test')]
				sourceSet.resources.exclude('**/*.java', '**/*.groovy')
			}

			project.afterEvaluate {
				if (project.tasks.names.contains('sourcesJar')) {
					project.tasks.named('sourcesJar', Jar) { jar ->
						jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
					}
				}
			}

			project.pluginManager.withPlugin('idea') {
				project.extensions.configure(IdeaModel) { model ->
					model.module.outputDir = project.file('build/classes/groovy/main')
					model.module.testOutputDir = project.file('build/classes/test')
				}
			}
		}
	}

	/**
	 * Configure distribution/bundling plugins if present.
	 */
	private void configureDistribution(Project project) {

		project.pluginManager.withPlugin('distribution') {
			project.extensions.getByType(DistributionContainer).named('main') { main ->
				main.contents { spec ->
					spec.from(project.tasks.named('jar').get().outputs.files)
					spec.from(project.tasks.named('groovydoc', Groovydoc).get())
						.into('groovydoc')
					spec.from(project.configurations.named('runtimeClasspath').get())
						.into('libraries')
					spec.from('source')
						.into('source')
					spec.from(project.rootDir)
						.include('CHANGELOG.md')
						.include('LICENSE.txt')
						.include('README.md')
				}
			}
			project.tasks.named('distTar', Task).configure { distTar ->
				distTar.enabled = false
			}
			project.tasks.named('distZip', Zip).configure { distZip ->
				distZip.dependsOn('groovydoc')
				distZip.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			}
		}
	}

	/**
	 * Fix groovydoc tasks and have them link to the right Java and Groovy docs
	 * when referencing core libraries.  Add a {@code groovydocJar} task to create
	 * a documentation artifact, replacing the {@code javadoc} one.
	 */
	private void configureGroovydocs(Project project) {

		project.pluginManager.withPlugin('groovy') {
			project.tasks.named('groovydoc', Groovydoc) { groovydoc ->
				groovydoc.link('https://docs.oracle.com/en/java/javase/21/docs/api/java.base/', 'java.', 'javax.', 'org.xml.')
				groovydoc.link('https://docs.groovy-lang.org/latest/html/gapi/', 'groovy.', 'org.apache.groovy.')
			}

			project.tasks.register('groovydocJar', Jar) { groovydocJar ->
				groovydocJar.description = 'Assembles a jar archive containing the main groovydoc.'
				groovydocJar.group = 'build'
				groovydocJar.from(project.tasks.named('groovydoc', Groovydoc).get().destinationDir)
				groovydocJar.destinationDirectory.set(project.file('build/libs'))
				groovydocJar.archiveClassifier.set('groovydoc')

				// TODO: Replacement option can maybe be deferred until the Maven publish step?
				project.pluginManager.withPlugin('distribution') {
					groovydocJar.archiveClassifier.set('javadoc')
				}
			}
			project.tasks.named('assemble') { assembleTask ->
				assembleTask.dependsOn('groovydocJar')
			}
		}
	}

	/**
	 * Adds the Maven Central and Maven Central Snapshots repositories to the
	 * project configuration.
	 */
	private void configureRepositories(Project project) {

		project.repositories.mavenCentral()
		project.repositories.maven {
			url = 'https://central.sonatype.com/repository/maven-snapshots/'
		}
	}

	/**
	 * Expands the {@code moduleVersion} property reference in the Groovy
	 * extension module manifest file, to the Gradle project version.
	 */
	private void configureResources(Project project) {

		project.pluginManager.withPlugin('groovy') {
			project.tasks.named('processResources', ProcessResources) { processResources ->
				processResources.filesMatching('**/org.codehaus.groovy.runtime.ExtensionModule') { file ->
					file.expand([moduleVersion: project.version])
				}
			}
		}
	}

	/**
	 * Configure verification plugins if present.
	 */
	private void configureVerification(Project project) {

		project.pluginManager.withPlugin('codenarc') {
			var sharedConfig = 'https://raw.githubusercontent.com/ultraq/codenarc-config-ultraq/master/codenarc.groovy'.toURL().text
			project.extensions.configure(CodeNarcExtension) { codenarc ->
				codenarc.config = project.resources.text.fromString(sharedConfig)
			}
		}

		project.pluginManager.withPlugin('jacoco') {
			project.tasks.withType(JacocoReport).configureEach { reportTask ->
				reportTask.reports.xml.required.set(true)
				reportTask.reports.html.required.set(true)
			}
		}
	}
}
