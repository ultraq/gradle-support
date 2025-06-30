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

import nz.net.ultraq.gradle.FluentConfigurationPlugin.FluentConfigurationExtension

import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests for checking that a Gradle project has been configured correctly.
 *
 * @author Emanuel Rabina
 */
class FluentConfigurationPluginIntegrationTests extends Specification {

	Project project
	FluentConfigurationExtension configure

	def setup() {
		project = ProjectBuilder.builder().build()
		project.pluginManager.apply('nz.net.ultraq.gradle.fluent-configuration')
		configure = project.extensions.getByType(FluentConfigurationExtension)
	}

	def "Configures a Groovy project with the Java version"() {
		when:
			configure.groovyProject().useJavaVersion(17)
		then:
			project.pluginManager.hasPlugin('groovy')
			project.java.toolchain.languageVersion.get() == JavaLanguageVersion.of(17)
	}

	def "Configures a combined source and resource directory"() {
		when:
			configure.groovyProject()
			configure.sourceSets()
				.withMainSourceDirectory('source')
				.withTestSourceDirectory('test')
		then:
			project.sourceSets.main.java.srcDirs == [project.file('source')] as Set
			project.sourceSets.test.java.srcDirs == [project.file('test')] as Set
	}

	def "Configures Maven Central and Snapshot repositories"() {
		when:
			configure.groovyProject()
			configure.repositories().useMavenCentralAndSnapshots()
		then:
			project.repositories.size() == 2
			project.repositories.named('MavenRepo').get() != null
			project.repositories.named('Maven Central Snapshots').get() != null
	}
}
