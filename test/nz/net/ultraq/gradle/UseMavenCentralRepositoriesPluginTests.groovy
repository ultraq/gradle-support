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

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Tests for adding Maven Central repositories via the plugin.
 *
 * @author Emanuel Rabina
 */
class UseMavenCentralRepositoriesPluginTests extends Specification {

	def "Adds Maven Central and Maven Central Snapshots repositories to a project"() {
		given:
			var project = ProjectBuilder.builder().build()
		when:
			project.pluginManager.apply('nz.net.ultraq.gradle.use-maven-central-repositories')
		then:
			project.repositories.size() == 2
			project.repositories.findByName('MavenRepo') != null
			project.repositories.find { it.url = 'https://central.sonatype.com/repository/maven-snapshots/' } != null
	}

	@Ignore("Gradle needs to give us a SettingsBuilder equivalent to test settings plugins")
	def "Adds Maven Central and Maven Central Snapshots repositories to all projects"() {
	}
}
