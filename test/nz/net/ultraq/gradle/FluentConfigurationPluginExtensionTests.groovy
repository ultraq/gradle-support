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
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests for the fluent configuration plugin extension.
 *
 * @author Emanuel Rabina
 */
class FluentConfigurationPluginExtensionTests extends Specification {

	Project project
	FluentConfigurationPluginExtension configure

	def setup() {
		project = ProjectBuilder.builder().build()
		project.pluginManager.apply('nz.net.ultraq.gradle.fluent-configuration')
		configure = project.extensions.getByName('configure') as FluentConfigurationPluginExtension
	}

	def "Returns a new Groovy project builder"() {
		when:
			var groovyProject = configure.createGroovyProject()
		then:
			groovyProject.class.simpleName.contains('_Decorated')
	}

	def "Returns an existing Groovy project builder"() {
		when:
			var groovyProject = configure.createGroovyProject()
			var existingGroovyProject = configure.asGroovyProject()
		then:
			existingGroovyProject == groovyProject
	}

	def "Throws an error if no Groovy project builder exists"() {
		when:
			configure.asGroovyProject()
		then:
			thrown(IllegalStateException)
	}

	def "Returns a Groovy application project builder"() {
		when:
			var groovyApplicationProject = configure.createGroovyApplicationProject() {
				mainClass = 'nz.net.ultraq.gradle.TestApplication'
			}
		then:
			groovyApplicationProject.class.simpleName.contains('_Decorated')
	}

	def "Returns an existing Groovy application project builder"() {
		when:
			var groovyApplicationProject = configure.createGroovyApplicationProject() {
				mainClass = 'nz.net.ultraq.gradle.TestApplication'
			}
			var existingGroovyApplicationProject = configure.asGroovyApplicationProject()
		then:
			existingGroovyApplicationProject == groovyApplicationProject
	}

	def "Throws an error if no Groovy application project builder exists"() {
		when:
			configure.asGroovyApplicationProject()
		then:
			thrown(IllegalStateException)
	}

	def "Returns a Groovy Gradle plugin project builder"() {
		when:
			var groovyGradlePluginProject = configure.createGroovyGradlePluginProject()
		then:
			groovyGradlePluginProject.class.simpleName.contains('_Decorated')
	}

	def "Returns an existing Groovy Gradle plugin project builder"() {
		when:
			var groovyGradlePluginProject = configure.createGroovyGradlePluginProject()
			var existingGroovyGradlePluginProject = configure.asGroovyGradlePluginProject()
		then:
			existingGroovyGradlePluginProject == groovyGradlePluginProject
	}

	def "Throws an error if no Groovy Gradle plugin project builder exists"() {
		when:
			configure.asGroovyGradlePluginProject()
		then:
			thrown(IllegalStateException)
	}

	def "Returns a Groovy library project builder"() {
		when:
			var groovyLibraryProject = configure.createGroovyLibraryProject()
		then:
			groovyLibraryProject.class.simpleName.contains('_Decorated')
	}

	def "Returns an existing Groovy library project builder"() {
		when:
			var groovyLibraryProject = configure.createGroovyLibraryProject()
			var existingGroovyLibraryProject = configure.asGroovyLibraryProject()
		then:
			existingGroovyLibraryProject == groovyLibraryProject
	}

	def "Throws an error if no Groovy library project builder exists"() {
		when:
			configure.asGroovyLibraryProject()
		then:
			thrown(IllegalStateException)
	}

	def "Returns a Maven publication builder"() {
		when:
			var mavenPublication = configure.createMavenPublication()
		then:
			mavenPublication.class.simpleName.contains('_Decorated')
	}

	def "Returns a ZIP distribution builder"() {
		when:
			var zipDistribution = configure.createZipDistribution()
		then:
			zipDistribution.class.simpleName.contains('_Decorated')
	}
}
