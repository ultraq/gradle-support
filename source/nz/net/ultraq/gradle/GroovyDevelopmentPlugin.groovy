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
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.signing.SigningExtension

/**
 * <p>Gradle plugin for my JVM-based projects.</p>
 *
 * <p>This plugin will apply several conventions, based on the plugins used in
 * the project.  It will always:</p>
 * <ul>
 *   <li>Add {@code MavenCentral} and {@code MavenSnapshot} repositories</li>
 * </ul>
 *
 * <p>If the {@code groovy} plugin is present, it will:</p>
 * <ul>
 *   <li>Configure {@code source} as the only Groovy source and resources
 *     directory</li>
 *   <li>Configure {@code test} as the only Groovy test source and resources
 *     directory</li>
 *   <li>Configure all test suites with {@code useJUnitJupiter}</li>
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

	private Project project

	@Override
	void apply(Project project) {

		configure(project)
			.directories()
			.groovydocs()
			.verification()
			.distribution()
			.publishing()
	}

	/**
	 * The entry point into the fluent API for configuring the project.
	 */
	private GroovyDevelopmentPlugin configure(Project project) {

		this.project = project
		return this
	}

	/**
	 * Set {@code source} and {@code test} as the combined source & resource
	 * directories for their respective sourcesets.
	 */
	private GroovyDevelopmentPlugin directories() {

		project.pluginManager.withPlugin('groovy') {
			project.pluginManager.withPlugin('idea') {
				project.extensions.configure(IdeaModel) { model ->
					model.module.outputDir = project.file('build/classes/groovy/main')
					model.module.testOutputDir = project.file('build/classes/test')
				}
			}
		}

		return this
	}

	/**
	 * Configure distribution/bundling plugins if present.
	 */
	private GroovyDevelopmentPlugin distribution() {

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

		return this
	}

	/**
	 * Fix groovydoc tasks and have them link to the right Java and Groovy docs
	 * when referencing core libraries.  Add a {@code groovydocJar} task to create
	 * a documentation artifact, replacing the {@code javadoc} one.
	 */
	private GroovyDevelopmentPlugin groovydocs() {

		project.pluginManager.withPlugin('groovy') {
			project.tasks.register('groovydocJar', Jar) { groovydocJar ->
				groovydocJar.description = 'Assembles a jar archive containing the main groovydoc.'
				groovydocJar.group = 'build'
				groovydocJar.dependsOn('groovydoc')
				groovydocJar.from(project.tasks.named('groovydoc', Groovydoc).get().destinationDir)
				groovydocJar.destinationDirectory.set(project.file('build/libs'))
				groovydocJar.archiveClassifier.set('groovydoc')
			}
			project.tasks.named('assemble') { assembleTask ->
				assembleTask.dependsOn('groovydocJar')
			}
		}

		return this
	}

	/**
	 * Configure the project for publishing to Maven Central, which includes
	 * setting plenty of metadata in the {@code pom.xml} file.
	 */
	private GroovyDevelopmentPlugin publishing() {

		// TODO: The signing, uploading, and automatic releasing, are probably better
		//       done using one of the existing plugins out there.

		project.pluginManager.withPlugin('maven-publish') {
			project.pluginManager.apply('signing')

			project.extensions.configure(JavaPluginExtension) { java ->
				java.withSourcesJar()
			}

			project.extensions.configure(PublishingExtension) { publishing ->
				publishing.publications.create('main', MavenPublication) { publication ->
					publication.from(project.components.named('java', SoftwareComponent).get())
					project.pluginManager.withPlugin('groovy') {
						publication.artifact(project.tasks.named('groovydocJar').get()) { artifact ->
							artifact.classifier = 'javadoc'
						}
					}
					publication.pom { pom ->
						pom.name.set(project.name)
						pom.description.set(project.description)
						pom.url.set("https://github.com/ultraq/${project.rootProject.name}")
						pom.licenses { licences ->
							licences.license { license ->
								license.name.set('The Apache Software License, Version 2.0')
								license.url.set('https://www.apache.org/licenses/LICENSE-2.0.txt')
								license.distribution.set('repo')
							}
						}
						pom.scm { scm ->
							scm.connection.set("scm:git:git@github.com:ultraq/${project.rootProject.name}.git")
							scm.developerConnection.set("scm:git:git@github.com:ultraq/${project.rootProject.name}.git")
							scm.url.set("https://github.com/ultraq/${project.rootProject.name}")
						}
						pom.developers { developers ->
							developers.developer { developer ->
								developer.name.set('Emanuel Rabina')
								developer.email.set('emanuelrabina@gmail.com')
								developer.url.set('http://www.ultraq.net.nz/')
							}
						}
					}
					project.extensions.configure(SigningExtension) { signing ->
						signing.sign(publication)
					}
				}
//				publishing.repositories { repositories ->
//					repositories.maven { maven ->
//						maven.url = project.version.endsWith('SNAPSHOT') ?
//							'https://central.sonatype.com/repository/maven-snapshots/' :
//							'https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/'
//						maven.credentials { credentials ->
//							credentials.username = project.property('sonatypeUsername')
//							credentials.password = project.property('sonatypePassword')
//						}
//					}
//				}
			}
		}

		return this
	}

	/**
	 * Configure verification plugins if present.
	 */
	private GroovyDevelopmentPlugin verification() {

		project.pluginManager.withPlugin('codenarc') {
			var sharedConfig = 'https://raw.githubusercontent.com/ultraq/codenarc-config-ultraq/master/codenarc.groovy'.toURL().text
			project.extensions.configure(CodeNarcExtension) { codenarc ->
				codenarc.config = project.resources.text.fromString(sharedConfig)
			}
		}

		return this
	}
}
