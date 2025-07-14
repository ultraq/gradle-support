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

import nz.net.ultraq.gradle.FluentConfigurationPlugin.FluentConfigurationPluginExtension

import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.jacoco.tasks.JacocoReport
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Tests for checking that a Gradle project has been configured correctly.
 *
 * @author Emanuel Rabina
 */
class FluentConfigurationPluginTests extends Specification {

	Project project
	FluentConfigurationPluginExtension configure

	def setup() {
		project = ProjectBuilder.builder().build()
		project.pluginManager.apply('nz.net.ultraq.gradle.fluent-configuration')
		configure = project.extensions.getByType(FluentConfigurationPluginExtension)
	}

	// @formatter:off
	def "Configures a Groovy project"() {
		when:
			configure.createGroovyProject()
		then:
			project.pluginManager.hasPlugin('groovy')
	}

	def "Configures a Groovy library project"() {
		when:
			configure.createGroovyLibrary()
		then:
			project.pluginManager.hasPlugin('java-library')
			project.pluginManager.hasPlugin('groovy')
	}

	def "Use the specified Java version"(int version) {
		when:
			configure.createGroovyLibrary()
				.useJavaVersion(version)
		then:
			project.java.toolchain.languageVersion.get() == JavaLanguageVersion.of(version)
			var links = project.tasks.named('groovydoc', Groovydoc).get().links
			links.size() == 2
			verifyAll(links.first()) {
				url == 'https://docs.groovy-lang.org/latest/html/gapi/'
				packages == ['groovy.', 'org.apache.groovy.']
			}
			verifyAll(links.last()) {
				url == "https://docs.oracle.com/en/java/javase/${version}/docs/api/java.base/"
				packages == ['java.', 'javax.']
			}
		where:
			version << [17, 21]
	}

	def "Configure Groovy compilation options"() {
		when:
			configure.createGroovyLibrary()
				.withCompileOptions() {
					groovyOptions.parameters = true
				}
		then:
			project.tasks.named('compileGroovy', GroovyCompile).get().groovyOptions.parameters
	}

	def "Configure groovydoc options"() {
		when:
			configure.createGroovyLibrary()
				.withGroovydocOptions() {
					overviewText = project.resources.text.fromString('Hello!')
				}
		then:
			project.tasks.named('groovydoc', Groovydoc).get().overviewText.asString() == 'Hello!'
	}

	def "Configures a combined source and resource directory"() {
		when:
			configure.createGroovyLibrary()
				.configureSource()
					.withSourceDirectory('source')
				.configureTesting()
					.withTestDirectory('test')
		then:
			project.sourceSets.main.java.srcDirs == [project.file('source')] as Set
			project.sourceSets.test.java.srcDirs == [project.file('test')] as Set
	}

	def "Configures Maven Central and Snapshot repositories"() {
		when:
			configure.createGroovyLibrary()
				.useMavenCentralRepositories()
		then:
			project.repositories.size() == 2
			project.repositories.findByName('MavenRepo') != null
			project.repositories.find { it.url = 'https://central.sonatype.com/repository/maven-snapshots/' } != null
	}

	def "Adds the codenarc plugin and uses the specified config file"() {
		when:
			configure.createGroovyProject()
				.configureTesting()
					.useCodenarc(project.resources.text.fromString('Hello!'))
		then:
			project.pluginManager.hasPlugin('codenarc')
			project.codenarc.config.asString() == 'Hello!'
	}

	@Ignore("Can't figure out what changes to assert on")
	def "Configures JUnit Jupiter for testing"() {
		when:
			configure.createGroovyLibrary()
				.configureTesting()
					.withTestDirectory('test')
					.useJUnitJupiter()
		then:
			project.testing.suites.test.dependencies
				.find { it.group == 'org.junit.jupiter' && it.name == 'junit-jupiter' } != null
	}

	def "Adds dependencies through their respective methods"() {
		when:
			configure.createGroovyLibrary()
				.configureSource()
					.withDependencies() {
						implementation 'org.apache.groovy:groovy:4.0.27'
					}
				.configureTesting()
					.withTestDependencies() {
						testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
					}
		then:
			project.configurations.named('implementation').get().dependencies
				.find { it.group == 'org.apache.groovy' && it.name == 'groovy' } != null
			project.configurations.named('testImplementation').get().dependencies
				.find { it.group == 'org.spockframework' && it.name == 'spock-core' } != null
	}

	def "Adds the Jacoco plugin with support for codecov"() {
		when:
			configure.createGroovyLibrary()
				.configureTesting()
					.useJacoco()
		then:
			project.pluginManager.hasPlugin('jacoco')
			JacocoReport jacocoTestReportTask = project.tasks.getByName('jacocoTestReport')
			jacocoTestReportTask.dependsOn.contains('test')
			jacocoTestReportTask.reports.xml.required.get() == true
			project.tasks.getByName('test').finalizedBy.mutableValues.contains('jacocoTestReport')
	}

	def "Creates a Maven publication with a 'main' item"() {
		when:
			configure.createMavenPublication()
		then:
			project.pluginManager.hasPlugin('maven-publish')
			var publications = project.extensions.getByType(PublishingExtension).publications
			verifyAll(publications) {
				size() == 1
				first().name == 'main'
			}
	}

	def "Adds and configures the main Java JAR"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.addJar() {
					manifest {
						attributes('Automatic-Module-Name': 'nz.net.ultraq.gradle.support')
					}
				}
		then:
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			verifyAll(publication.component.get()) {
				it instanceof SoftwareComponent
				it.name == 'java'
			}
			var jar = project.tasks.named('jar', Jar).get()
			jar.manifest.attributes['Automatic-Module-Name'] == 'nz.net.ultraq.gradle.support'
	}

	def "Adds the main sources JAR"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.addSourcesJar()
		then:
			var jar = project.tasks.named('sourcesJar', Jar).get()
			jar.duplicatesStrategy == DuplicatesStrategy.EXCLUDE
	}

	def "Adds a groovydocJar task and includes it in the main bundle"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.addGroovydocJar()
		then:
			var groovydocJar = project.tasks.named('groovydocJar', Jar).get()
			verifyAll(groovydocJar) {
				group == 'build'
				dependsOn.contains('groovydoc')
				destinationDirectory.get().toString() == project.file("${project.layout.buildDirectory}/libs").toString()
				archiveClassifier.get() == 'javadoc'
			}
			project.tasks.named('assemble').get().dependsOn.contains(groovydocJar)
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			publication.artifacts.find { it.classifier == 'javadoc' } != null
	}

	def "POM configuration is just a wrapper for the publication pom closure"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.configurePom() {
					inceptionYear = '2025'
				}
		then:
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			publication.pom.inceptionYear.get() == '2025'
	}

	def "Fills in an Apache 2.0 License"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.configurePom()
				.useApache20License()
		then:
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			publication.pom.licenses.size() == 1
			verifyAll(publication.pom.licenses.first()) {
				name.get() == 'The Apache Software License, Version 2.0'
				url.get() == 'https://www.apache.org/licenses/LICENSE-2.0.txt'
				distribution.get() == 'repo'
			}
	}

	def "Fills in GitHub SCM details"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.configurePom()
				.withGitHubScm('ultraq', 'gradle-support')
		then:
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			verifyAll(publication.pom.scm) {
				connection.get() == 'scm:git:git@github.com:ultraq/gradle-support.git'
				developerConnection.get() == 'scm:git:git@github.com:ultraq/gradle-support.git'
				url.get() == 'https://github.com/ultraq/gradle-support'
			}
	}

	def "Adds developer details"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.configurePom()
				.withDevelopers([
					name: 'Emanuel Rabina',
					email: 'emanuelrabina@gmail.com',
					url: 'https://www.ultraq.net.nz'
				])
		then:
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			publication.pom.developers.size() == 1
			verifyAll(publication.pom.developers.first()) {
				name.get() == 'Emanuel Rabina'
				email.get() == 'emanuelrabina@gmail.com'
				url.get() == 'https://www.ultraq.net.nz'
			}
	}

	def "Publishes to any Maven repository"() {
		when:
			configure.createGroovyLibrary()
			configure.createMavenPublication()
				.publishTo {
					name = 'My local repo'
					url = project.file('../my-local-repo')
				}
		then:
			project.extensions.getByType(PublishingExtension).repositories.findByName('My local repo') != null
//			localRepo.url == project.file('../my-local-repo')
	}
	// @formatter:on
}
