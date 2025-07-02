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

import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testfixtures.ProjectBuilder
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

	def "Configures a Groovy project with the Java version"() {
		when:
			configure.createGroovyProject()
				.useJavaVersion(17)
		then:
			project.pluginManager.hasPlugin('groovy')
			project.java.toolchain.languageVersion.get() == JavaLanguageVersion.of(17)
	}

	def "Configures a combined source and resource directory"() {
		when:
			configure.createGroovyProject()
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
			configure.createGroovyProject()
				.useMavenCentralAndSnapshots()
		then:
			project.repositories.size() == 2
			project.repositories.named('MavenRepo').get() != null
			project.repositories.named('Maven Central Snapshots').get() != null
	}

	@Ignore("Can't figure out what changes to assert on")
	def "Configures JUnit Jupiter for testing"() {
		when:
			configure.createGroovyProject()
				.configureTesting()
					.withTestDirectory('test')
					.useJUnitJupiter()
		then:
			project.testing.suites.test.dependencies
				.find { it.group == 'org.junit.jupiter' && it.name == 'junit-jupiter' } != null
	}

	def "Adds dependencies through their respective methods"() {
		when:
			configure.createGroovyProject()
				.withDependencies {
					implementation 'org.apache.groovy:groovy:4.0.27'
				}
				.configureTesting()
					.withTestDependencies {
						testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
					}
		then:
			project.configurations.named('implementation').get().dependencies
				.find { it.group == 'org.apache.groovy' && it.name == 'groovy' } != null
			project.configurations.named('testImplementation').get().dependencies
				.find { it.group == 'org.spockframework' && it.name == 'spock-core' } != null
	}
}
