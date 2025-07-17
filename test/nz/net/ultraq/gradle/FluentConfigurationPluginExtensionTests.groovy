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
import org.gradle.api.Task
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.internal.file.copy.DefaultCopySpec
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip
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

	def "Starts configuration of a Groovy project"() {
		when:
			configure.createGroovyProject()
		then:
			project.pluginManager.hasPlugin('groovy')
	}

	def "Starts configuration of a Groovy library project"() {
		when:
			configure.createGroovyLibraryProject()
		then:
			project.pluginManager.hasPlugin('java-library')
			project.pluginManager.hasPlugin('groovy')
	}

	def "Starts configuration of a Groovy Gradle plugin project"() {
		when:
			configure.createGroovyGradlePluginProject()
		then:
			project.pluginManager.hasPlugin('groovy-gradle-plugin')
	}

	def "Starts configuration of a Maven publication"() {
		when:
			configure.createMavenPublication()
		then:
			project.pluginManager.hasPlugin('maven-publish')
			var publishingExtension = project.extensions.getByName('publishing') as PublishingExtension
			publishingExtension.publications.named('main', MavenPublication)
	}

	def "Starts configuration of a ZIP distribution"() {
		when:
			configure.createZipDistribution()
		then:
			project.pluginManager.hasPlugin('distribution')
			var mainDistribution = project.extensions.getByType(DistributionContainer).named('main').get()

			// TODO: Is there a way to do the below without reaching into Gradle internals?
			var copySpec = (mainDistribution.contents as CopySpecInternal).children.first() as DefaultCopySpec
			verifyAll(copySpec.patterns.includes) {
				contains('CHANGELOG.md')
				contains('LICENSE.txt')
				contains('README.md')
			}

			!project.tasks.named('distTar', Task).get().enabled
			project.tasks.named('distZip', Zip).get().duplicatesStrategy == DuplicatesStrategy.EXCLUDE
	}

	def "Starts configuration of a ZIP distribution - with Groovy project"() {
		when:
			configure.createGroovyProject()
			configure.createZipDistribution()
		then:
			project.tasks.named('distZip', Zip).get().dependsOn.contains('groovydoc')
	}
}
