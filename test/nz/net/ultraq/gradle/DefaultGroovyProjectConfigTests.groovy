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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Tests for configuring a Groovy project.
 *
 * @author Emanuel Rabina
 */
class DefaultGroovyProjectConfigTests extends Specification {

	Project project
	GroovyProjectConfig config

	def setup() {
		project = ProjectBuilder.builder().build()
		config = new DefaultGroovyProjectConfig(project)
	}

	def "Use the specified Java version"(int version) {
		when:
			config.useJavaVersion(version)
		then:
			var java = project.extensions.getByName('java') as JavaPluginExtension
			java.toolchain.languageVersion.get() == JavaLanguageVersion.of(version)
			var links = project.tasks.named('groovydoc', Groovydoc).get().links
			verifyAll(links) {
				size() == 2
				verifyAll(first()) {
					url == 'https://docs.groovy-lang.org/latest/html/gapi/'
					packages == ['groovy.', 'org.apache.groovy.']
				}
				verifyAll(last()) {
					url == "https://docs.oracle.com/en/java/javase/${version}/docs/api/java.base/"
					packages == ['java.', 'javax.']
				}
			}
		where:
			version << [17, 21]
	}

	def "Configure Java compilation options"() {
		when:
			config.withJavaCompileOptions() {
				sourceCompatibility = '17'
			}
		then:
			project.tasks.named('compileJava', JavaCompile).get().sourceCompatibility == '17'
	}

	def "Configure Groovy compilation options"() {
		when:
			config.withGroovyCompileOptions() {
				groovyOptions.parameters = true
			}
		then:
			project.tasks.named('compileGroovy', GroovyCompile).get().groovyOptions.parameters
	}

	def "Configure groovydoc options"() {
		when:
			config.withGroovydocOptions() {
				overviewText = project.resources.text.fromString('Hello!')
			}
		then:
			project.tasks.named('groovydoc', Groovydoc).get().overviewText.asString() == 'Hello!'
	}

	def "Configures the jar task"() {
		when:
			config.withJarOptions {
				manifest {
					attributes 'Automatic-Module-Name': 'nz.net.ultraq.gradle.support'
				}
			}
		then:
			var jar = project.tasks.named('jar', Jar).get()
			jar.manifest.attributes['Automatic-Module-Name'] == 'nz.net.ultraq.gradle.support'
	}

	def "Adds a sourcesJar task"() {
		when:
			config.withSourcesJar()
		then:
			project.tasks.named('sourcesJar', Jar)
	}

	def "Adds a groovydocJar task"() {
		when:
			project.pluginManager.apply('groovy')
			config.withGroovydocJar()
		then:
			var groovydocJar = project.tasks.named('groovydocJar', Jar).get()
			verifyAll(groovydocJar) {
				group == 'build'
				dependsOn.contains('groovydoc')
				destinationDirectory.get() == project.layout.buildDirectory.dir('libs').get()
				archiveClassifier.get() == 'javadoc'
			}
			project.tasks.named('assemble').get().dependsOn.contains(groovydocJar)
	}

	def "Applies the shadow JAR plugin and configures it"() {
		when:
			config.withShadowJar {
				archiveClassifier.set('test')
			}
		then:
			project.pluginManager.hasPlugin('com.gradleup.shadow')
			project.tasks.named('shadowJar', ShadowJar).get().archiveClassifier.get() == 'test'
	}

	def "Configures a combined source and resource directory"() {
		when:
			// @formatter:off
			config
				.configureSource()
					.withSourceDirectory('source')
				.configureTesting()
					.withTestDirectory('test')
			// @formatter:on
		then:
			var sourceSets = project.extensions.getByName('sourceSets') as SourceSetContainer
			sourceSets.named('main').get().java.srcDirs == [project.file('source')] as Set
			sourceSets.named('test').get().java.srcDirs == [project.file('test')] as Set
			project.tasks.named('jar', Jar).get().duplicatesStrategy == DuplicatesStrategy.EXCLUDE
	}

	def "Configures Maven Central and Snapshot repositories"() {
		when:
			config.useMavenCentralRepositories()
		then:
			verifyAll(project.repositories) {
				size() == 2
				named('MavenRepo')
				var snapshotsRepository = named('Maven Central Snapshots', MavenArtifactRepository).get()
				snapshotsRepository.url == 'https://central.sonatype.com/repository/maven-snapshots/'.toURI()
			}
	}

	def "Adds the codenarc plugin and uses the specified config file"() {
		when:
			// @formatter:off
			config
				.configureTesting()
					.useCodenarc(project.resources.text.fromString('Hello!'))
			// @formatter:on
		then:
			project.pluginManager.hasPlugin('codenarc')
			var codeNarc = project.extensions.getByName('codenarc') as CodeNarcExtension
			codeNarc.config.asString() == 'Hello!'
	}

	@Ignore("Can't figure out what changes to assert on")
	def "Configures JUnit Jupiter for testing"() {
		when:
			// @formatter:off
			config
				.configureTesting()
					.withTestDirectory('test')
					.useJUnitJupiter()
			// @formatter:on
		then:
			var testing = project.extensions.getByName('testing') as TestingExtension
			var testSuite = testing.suites.named('test').get()
			testSuite.dependencies.find { it.group == 'org.junit.jupiter' && it.name == 'junit-jupiter' } != null
	}

	def "Adds dependencies through their respective methods"() {
		when:
			// @formatter:off
			config
				.configureSource()
					.withDependencies() {
						implementation 'org.apache.groovy:groovy:4.0.27'
					}
				.configureTesting()
					.withTestDependencies() {
						testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
					}
			// @formatter:on
		then:
			project.configurations.named('implementation').get().dependencies
				.find { it.group == 'org.apache.groovy' && it.name == 'groovy' } != null
			project.configurations.named('testImplementation').get().dependencies
				.find { it.group == 'org.spockframework' && it.name == 'spock-core' } != null
	}

	def "Adds the Jacoco plugin with support for codecov"() {
		when:
			// @formatter:off
			config
				.configureTesting()
					.useJacoco()
			// @formatter:on
		then:
			project.pluginManager.hasPlugin('jacoco')
			var jacocoTestReportTask = project.tasks.named('jacocoTestReport').get() as JacocoReport
			verifyAll(jacocoTestReportTask) {
				dependsOn.contains('test')
				reports.xml.required.get()
			}
			// TODO: Can we also not rely on internals here?
			(project.tasks.named('test').get().finalizedBy as DefaultTaskDependency).mutableValues.contains('jacocoTestReport')
	}
}
