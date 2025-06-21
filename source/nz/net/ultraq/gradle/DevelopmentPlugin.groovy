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
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.plugins.quality.CodeNarcPlugin
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer

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
class DevelopmentPlugin implements Plugin<Project> {

	protected List<String> sourceDirectories = ['source']
	protected List<String> testDirectories = ['test']

	@Override
	void apply(Project project) {

		configureRepositories(project)
		configureDirectories(project)
		configureResourceProcessing(project)
		configureGroovydocs(project)
		configureVerification(project)
		configureDistribution(project)
	}

	/**
	 * Adjust source and build directories, and any standard Gradle tasks that
	 * rely on them.
	 */
	private void configureDirectories(Project project) {

		project.pluginManager.withPlugin('java') {
			var sourceSets = project.extensions.getByType(SourceSetContainer)
			sourceSets.named('main').configure { sourceSet ->
				sourceSet.java.srcDirs = sourceDirectories
				sourceSet.resources.srcDirs = sourceDirectories
				sourceSet.resources.exclude('**/*.java')
			}
			sourceSets.named('test').configure { sourceSet ->
				sourceSet.java.srcDirs = testDirectories
				sourceSet.resources.srcDirs = testDirectories
			}

			project.pluginManager.withPlugin('idea') { IdeaPlugin ideaPlugin ->
				ideaPlugin.model.module.outputDir = project.file('build/classes/java/main')
				ideaPlugin.model.module.testOutputDir = project.file('build/classes/test')
			}
		}

		project.pluginManager.withPlugin('groovy') { GroovyPlugin groovyPlugin ->
			var sourceSets = project.extensions.getByType(SourceSetContainer)
			sourceSets.named('main').configure { sourceSet ->
				sourceSet.extensions.getByType(GroovySourceDirectorySet).srcDirs = sourceDirectories
				sourceSet.resources.exclude('**/*.groovy')
			}
			sourceSets.named('test').configure { sourceSet ->
				sourceSet.extensions.getByType(GroovySourceDirectorySet).srcDirs = testDirectories
				sourceSet.resources.srcDirs = testDirectories
			}

			project.pluginManager.withPlugin('idea') { IdeaPlugin ideaPlugin ->
				ideaPlugin.model.module.outputDir = project.file('build/classes/groovy/main')
				ideaPlugin.model.module.testOutputDir = project.file('build/classes/test')
			}
		}
	}

	/**
	 * Configure distribution/bundling plugins if present.
	 */
	private void configureDistribution(Project project) {

		project.pluginManager.withPlugin('distribution') { DistributionPlugin distributionPlugin ->
			project.extensions.getByType(DistributionContainer).named('main') { main ->
				main.contents { spec ->
					project.tasks.named('jar').configure { jar ->
						spec.from(jar.outputs.files)
					}
					project.tasks.named('javadoc').configure { javadoc ->
						spec.from(javadoc).into('javadoc')
					}
					project.tasks.named('groovydoc', Groovydoc).configure { groovydoc ->
						spec.from(groovydoc.destinationDir).into('groovydoc')
					}
					project.configurations.named('runtimeClasspath').configure { runtimeClasspath ->
						spec.from(runtimeClasspath).into('libraries')
					}
					spec.from(sourceDirectories).into('source')
					spec.from(project.rootDir)
						.include('CHANGELOG.md')
						.include('LICENSE.txt')
						.include('README.md')
				}
			}
			project.tasks.named('distTar', Task).configure { task ->
				task.enabled = false
			}
			project.tasks.named('distZip', Zip).configure { task ->
				task.dependsOn('javadoc')
				task.dependsOn('groovydoc')
				task.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			}
		}
	}

	/**
	 * Fix groovydoc tasks and have them link to the right Java and Groovy docs
	 * when referencing core libraries.  Add a {@code groovydocJar} task to create
	 * a documentation artifact, replacing the {@code javadoc} one.
	 */
	private void configureGroovydocs(Project project) {

		project.pluginManager.withPlugin('groovy') { GroovyPlugin groovyPlugin ->
			// TODO: Maybe not needed any more with Gradle 9?
			// Fix for NoClassDefFoundError when running groovydoc via Gradle and using Groovy 4.0.2+
//		dependencies {
//			compileOnly 'com.github.javaparser:javaparser-symbol-solver-core:3.26.4'
//		}

			project.tasks.named('groovydoc', Groovydoc).configure { groovydoc ->
				groovydoc.link('http://docs.oracle.com/javase/8/docs/api/', 'java.', 'javax.', 'org.xml.')
				groovydoc.link('http://docs.groovy-lang.org/latest/html/gapi/', 'groovy.', 'org.codehaus.groovy.')

				project.tasks.register('groovydocJar', Jar) { groovydocJar ->
					groovydocJar.from(groovydoc.destinationDir)
					groovydocJar.destinationDirectory.set(project.file('build/libs'))
					groovydocJar.archiveClassifier.set('javadoc')
				}
			}
		}
	}

	/**
	 * Include Maven central and snapshot repositories by default.
	 */
	private void configureRepositories(Project project) {

		project.repositories.mavenCentral()
		project.repositories.maven(repo -> {
			repo.url = 'https://central.sonatype.com/repository/maven-snapshots/'
		})
	}

	/**
	 * Expand any {@code ${moduleVersion}} placeholders in extension manifests.
	 */
	private void configureResourceProcessing(Project project) {

		project.pluginManager.withPlugin('groovy') { GroovyPlugin groovyPlugin ->
			project.tasks.named('processResources', ProcessResources).configure { processResources ->
				processResources.filesMatching('**/org.codehaus.groovy.runtime.ExtensionModule') { file ->
					file.expand([
						moduleVersion: project.version
					])
				}
			}
		}
	}

	/**
	 * Configure verification plugins if present.
	 */
	private void configureVerification(Project project) {

		project.pluginManager.withPlugin('codenarc') { CodeNarcPlugin codeNarcPlugin ->
			var sharedConfig = 'https://raw.githubusercontent.com/ultraq/codenarc-config-ultraq/master/codenarc.groovy'.toURL().text
			project.extensions.getByType(CodeNarcExtension).config = project.resources.text.fromString(sharedConfig)
		}

		project.pluginManager.withPlugin('jacoco') { JacocoPlugin jacocoPlugin ->
			project.tasks.withType(JacocoReport).configureEach { reportTask ->
				reportTask.reports { JacocoReportsContainer reportsContainer ->
					reportsContainer.xml.required.set(true)
					reportsContainer.html.required.set(true)
				}
			}
		}
	}
}
