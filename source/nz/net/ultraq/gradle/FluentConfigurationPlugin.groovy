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

import nz.net.ultraq.gradle.fluent.GroovyProjectConfig
import nz.net.ultraq.gradle.fluent.MavenCentralConfig
import nz.net.ultraq.gradle.fluent.MavenPomConfig
import nz.net.ultraq.gradle.fluent.MavenPublicationConfig
import nz.net.ultraq.gradle.fluent.SourceConfig
import nz.net.ultraq.gradle.fluent.TestingConfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.signing.SigningExtension
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

import groovy.transform.TupleConstructor

/**
 * Adds a {@code configure} script block to a {@code build.gradle} file, within
 * which a fluent API can be used to configure a project.
 *
 * @author Emanuel Rabina
 */
class FluentConfigurationPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.extensions.create('configure', FluentConfigurationPluginExtension, project)
	}

	/**
	 * The {@code configure} script block and the entry-point methods for the fluent
	 * API.
	 */
	@TupleConstructor(defaults = false)
	static abstract class FluentConfigurationPluginExtension {

		final Project project

		/**
		 * Starts a fluent chain for configuring a Groovy library project.  This
		 * will apply the {@code groovy} and {@code java-library} plugins, and
		 * configure the {@code groovydoc} task to generate docs with links to any
		 * Groovy SDK libraries (those starting with {@code groovy.} or
		 * {@code org.apache.groovy.}).
		 */
		GroovyProjectConfig createGroovyLibrary() {

			project.pluginManager.apply('java-library')
			return new DefaultGroovyProjectConfig()
		}

		/**
		 * Starts a fluent chain for configuring publishing artifacts to a Maven
		 * repository.  This will apply the {@code maven-publish} plugin and create
		 * a {@code main} publication, which all of the methods in this chain will
		 * operate on.
		 */
		MavenPublicationConfig createMavenPublication() {

			return new DefaultMavenPublicationConfig()
		}

		private class DefaultGroovyProjectConfig implements GroovyProjectConfig, SourceConfig, TestingConfig {

			DefaultGroovyProjectConfig() {

				project.pluginManager.apply('groovy')
				project.tasks.named('groovydoc', Groovydoc) { groovydoc ->
					groovydoc.link('https://docs.groovy-lang.org/latest/html/gapi/', 'groovy.', 'org.apache.groovy.')
				}
			}

			@Override
			SourceConfig expand(String filePattern, Map<String, String> replacements) {

				project.tasks.named('processResources', ProcessResources) { processResources ->
					processResources.filesMatching(filePattern) { file ->
						file.expand(replacements)
					}
				}
				return this
			}

			@Override
			SourceConfig expandExtensionModuleVersion(String propertyName = 'moduleVersion', String value = project.version) {

				return expand('**/org.codehaus.groovy.runtime.ExtensionModule', [(propertyName): value])
			}

			@Override
			SourceConfig configureSource() {

				return this
			}

			@Override
			TestingConfig configureTesting() {

				return this
			}

			@Override
			GroovyProjectConfig useMavenCentralRepositories() {

				project.pluginManager.apply(UseMavenCentralRepositoriesPlugin)
				return this
			}

			@Override
			GroovyProjectConfig withCompileOptions(@DelegatesTo(GroovyCompile) Closure configure) {

				project.tasks.named('compileGroovy', GroovyCompile, configure)
				return this
			}

			@Override
			GroovyProjectConfig withGroovydocOptions(@DelegatesTo(Groovydoc) Closure configure) {

				project.tasks.named('groovydoc', Groovydoc, configure)
				return this
			}

			@Override
			TestingConfig useJacoco() {

				project.pluginManager.apply('jacoco')
				project.tasks.named('test').configure { test ->
					test.finalizedBy('jacocoTestReport')
				}
				project.tasks.named('jacocoTestReport', JacocoReport) { jacocoTestReport ->
					jacocoTestReport.dependsOn('test')
					jacocoTestReport.reports { reports ->
						reports.xml.required.set(true)
					}
				}
				return this
			}

			@Override
			GroovyProjectConfig useJavaVersion(int version) {

				project.extensions.configure(JavaPluginExtension) { java ->
					java.toolchain.languageVersion.set(JavaLanguageVersion.of(version))
				}
				project.tasks.named('groovydoc', Groovydoc) { groovydoc ->
					groovydoc.link("https://docs.oracle.com/en/java/javase/${version}/docs/api/java.base/", 'java.', 'javax.')
				}
				return this
			}

			@Override
			TestingConfig useJUnitJupiter() {

				project.extensions.configure(TestingExtension) { testing ->
					testing.suites.configureEach { JvmTestSuite test ->
						test.useJUnitJupiter()
					}
				}
				return this
			}

			@Override
			SourceConfig withDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

				project.dependencies(configure)
				return this
			}

			/**
			 * Sets a single source directory for both source and resource files in
			 * the named sourceset.
			 */
			private void withDirectoryForSourceSetAt(Object path, String name) {

				project.extensions.configure(SourceSetContainer) { sourceSets ->
					sourceSets.named(name) { sourceSet ->
						[sourceSet.java, sourceSet.extensions.getByType(GroovySourceDirectorySet), sourceSet.resources]*.srcDirs =
							[project.file(path)]
						sourceSet.resources.exclude('**/*.java', '**/*.groovy')
					}
				}
			}

			@Override
			SourceConfig withSourceDirectory(Object path) {

				withDirectoryForSourceSetAt(path, 'main')
				return this
			}

			@Override
			TestingConfig withTestDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

				project.dependencies(configure)
				return this
			}

			@Override
			TestingConfig withTestDirectory(Object path) {

				withDirectoryForSourceSetAt(path, 'test')
				return this
			}
		}

		private class DefaultMavenPublicationConfig implements MavenPublicationConfig, MavenPomConfig, MavenCentralConfig {

			private final PublishingExtension publishing
			private final MavenPublication publication

			DefaultMavenPublicationConfig() {

				project.pluginManager.apply('maven-publish')
				publishing = project.extensions.getByType(PublishingExtension)
				publication = publishing.publications.create('main', MavenPublication)
			}

			@Override
			MavenPublicationConfig addGroovydocJar() {

				if (!project.pluginManager.hasPlugin('groovy')) {
					throw new IllegalStateException(
						'Cannot add groovydocJar task on a non-Groovy project.  Be sure to ' +
						'add the groovy plugin first, or to have configured a groovy project ' +
						'using createGroovyProject().'
					)
				}

				var groovydocJar = project.tasks.register('groovydocJar', Jar) { groovydocJar ->
					groovydocJar.description = 'Assembles a jar archive containing the main groovydoc.'
					groovydocJar.group = 'build'
					groovydocJar.dependsOn('groovydoc')
					groovydocJar.from(project.tasks.named('groovydoc', Groovydoc).get().destinationDir)
					groovydocJar.destinationDirectory.set(project.file("${project.layout.buildDirectory}/libs"))
					groovydocJar.archiveClassifier.set('javadoc')
				}
				project.tasks.named('assemble') { assembleTask ->
					assembleTask.dependsOn(groovydocJar.get())
				}
				publication.artifact(groovydocJar.get()) { artifact ->
					artifact.classifier = 'javadoc'
				}

				return this
			}

			@Override
			MavenPublicationConfig addJar(@DelegatesTo(Jar) Closure configure = null) {

				publication.from(project.components.named('java', SoftwareComponent).get())
				if (configure) {
					project.tasks.named('jar', Jar, configure)
				}
				return this
			}

			@Override
			MavenPublicationConfig addSourcesJar() {

				project.extensions.configure(JavaPluginExtension) { java ->
					java.withSourcesJar()
					project.tasks.named('sourcesJar', Jar) { jar ->
						jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
					}
				}
				return this
			}

			@Override
			MavenPomConfig configurePom(@DelegatesTo(MavenPom) Closure configure = null) {

				publication.pom { pom ->
					pom.name.set(project.name)
					pom.description.set(project.description)
					if (configure) {
						configure.delegate = pom
						configure()
					}
				}
				return this
			}

			@Override
			MavenPomConfig useApache20License() {

				publication.pom { pom ->
					pom.licenses { licences ->
						licences.license { license ->
							license.name.set('The Apache Software License, Version 2.0')
							license.url.set('https://www.apache.org/licenses/LICENSE-2.0.txt')
							license.distribution.set('repo')
						}
					}
				}
				return this
			}

			@Override
			MavenPomConfig withDevelopers(Map<String, String>... developers) {

				publication.pom { pom ->
					pom.developers { pomDeveloperSpec ->
						developers.each { developer ->
							pomDeveloperSpec.developer { pomDeveloper ->
								pomDeveloper.name.set(developer.name)
								pomDeveloper.email.set(developer.email)
								pomDeveloper.url.set(developer.url)
							}
						}
					}
				}
				return this
			}

			@Override
			MavenPomConfig withGitHubScm(String owner, String repository = project.name) {

				publication.pom { pom ->
					pom.scm { scm ->
						scm.connection.set("scm:git:git@github.com:${owner}/${repository}.git")
						scm.developerConnection.set("scm:git:git@github.com:${owner}/${repository}.git")
						scm.url.set("https://github.com/${owner}/${repository}")
					}
				}
				return this
			}

			@Override
			MavenCentralConfig publishTo(@DelegatesTo(MavenArtifactRepository) Closure configure) {

				publishing.repositories { repositories ->
					repositories.maven(configure)
				}
				return this
			}

			@Override
			MavenCentralConfig publishToMavenCentral(String username, String password) {

				project.pluginManager.apply('signing')
				project.extensions.configure(SigningExtension) { signing ->
					signing.sign(publication)
				}
				publishing.repositories { repositories ->
					repositories.maven { maven ->
						maven.url = project.version.endsWith('SNAPSHOT') ?
							'https://central.sonatype.com/repository/maven-snapshots/' :
							'https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/'
						maven.credentials { credentials ->
							credentials.username = username
							credentials.password = password
						}
					}
				}
				return this
			}
		}
	}
}
