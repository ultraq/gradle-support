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

import nz.net.ultraq.gradle.PublishingPluginExtension.License

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.signing.SigningExtension

/**
 * <p>Gradle plugin for publishing my JVM-based projects to Maven Central.</p>
 *
 * <p>This plugin will apply several conventions, based on the plugins used in
 * the project.  It works together with the {@code maven-publish} plugin, which
 * if present in the same project will make this plugin:</p>
 * <ul>
 *   <li>Apply the {@code signing} plugin</li>
 *   <li>Create a publication named "library"</li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
class PublishingPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.pluginManager.withPlugin('maven-publish') { mavenPublishPlugin ->
			project.pluginManager.apply('signing')
			var extension = configureExtension(project)
			configureArtifacts(project)
			configurePublishing(project, extension)
			configureSigning(project)
		}
	}

	/**
	 * Configure tasks to produce artifacts for publishing.
	 */
	private void configureArtifacts(Project project) {

		project.pluginManager.withPlugin('groovy') { groovyPlugin ->
			// TODO: If groovydocJar task available?
			project.tasks.named('assemble') { assembleTask ->
				assembleTask.dependsOn('groovydocJar')
			}
		}

		var java = project.extensions.getByType(JavaPluginExtension)
		project.pluginManager.withPlugin('java') { javaPlugin ->
			java.withJavadocJar()
		}
		java.withSourcesJar()

		project.tasks.named('sourcesJar', Jar) { sourcesJarTask ->
			sourcesJarTask.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}
	}

	/**
	 * Create and configure the extension object used to configure this plugin.
	 */
	private PublishingPluginExtension configureExtension(Project project) {

		var extension = project.extensions.create('publishingPlugin', PublishingPluginExtension)
		extension.licenses.convention([
			new License() {
				@Override
				Property<String> getName() {
					return project.objects.property(String).value('The Apache Software License, Version 2.0')
				}

				@Override
				Property<String> getUrl() {
					return project.objects.property(String).value('http://www.apache.org/licenses/LICENSE-2.0.txt')
				}
			}
		])
		return extension
	}

	/**
	 * Configure the pom.xml file for Maven Central.
	 */
	private void configurePublishing(Project project, PublishingPluginExtension extension) {

		var publishingExtension = project.extensions.getByType(PublishingExtension)
		publishingExtension.publications.create('library', MavenPublication) { publication ->
			publication.from(project.components.named('java').get())
			project.pluginManager.withPlugin('groovy') { groovyPlugin ->
				publication.artifact(project.tasks.named('groovydocJar', Jar).get()) { artifact ->
					artifact.classifier = 'javadoc'
				}
			}
			project.afterEvaluate {
				publication.pom { pom ->
					pom.name.set(project.name)
					pom.description.set(project.description)
					pom.url.set("https://github.com/ultraq/${project.rootProject.name}")
					pom.inceptionYear.set(extension.year)
					if (extension.licenses) {
						pom.licenses { licences ->
							extension.licenses.get().each { extLicence ->
								licences.license { license ->
									license.name.set(extLicence.name)
									license.url.set(extLicence.url)
								}
							}
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
							developer.url.set('https://www.ultraq.net.nz')
						}
					}
					if (extension.contributors) {
						pom.contributors { contributors ->
							extension.contributors.get().each { extContributor ->
								contributors.contributor { contributor ->
									contributor.name.set(extContributor.name)
									contributor.email.set(extContributor.email)
									contributor.url.set(extContributor.url)
								}
							}
						}
					}
				}
			}
		}
		publishingExtension.repositories { repositories ->
			repositories.maven { maven ->
				project.afterEvaluate {
					maven.url = project.version.toString().endsWith('SNAPSHOT') ?
						'https://central.sonatype.com/repository/maven-snapshots/' :
						'https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/'
				}
				maven.credentials { credentials ->
					credentials.username = project.findProperty('sonatypeUsername')
					credentials.password = project.findProperty('sonatypePassword')
				}
			}
		}
	}

	/**
	 * Configure the {@code signing} plugin.
	 */
	private void configureSigning(Project project) {

		var signingExtension = project.extensions.getByType(SigningExtension)
		var publishingExtension = project.extensions.getByType(PublishingExtension)
		signingExtension.sign(publishingExtension.publications.named('library', MavenPublication).get())
	}
}
