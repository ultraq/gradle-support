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
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

/**
 * Tests for adding Maven Central repositories via the plugin.
 *
 * @author Emanuel Rabina
 */
class UseMavenCentralRepositoriesPluginTests extends Specification {

	@TempDir
	File testProjectDir

	def "Adds Maven Central and Maven Central Snapshots repositories"() {
		given:
			var project = ProjectBuilder.builder().build()
		when:
			project.pluginManager.apply('nz.net.ultraq.gradle.use-maven-central-repositories')
		then:
			project.repositories.size() == 2
			project.repositories.findByName('MavenRepo') != null
			project.repositories.find { it.url = 'https://central.sonatype.com/repository/maven-snapshots/' } != null
	}

	// Would love to have something like ProjectBuilder for settings.  For now,
	// an integration test will have to do.
	def "Adds Maven Central and Maven Central Snapshots repositories to settings"() {
		given:
			var settingsFile = new File(testProjectDir, 'settings.gradle')
			settingsFile << """
				plugins {
          id 'nz.net.ultraq.gradle.use-maven-central-repositories'
        }
        rootProject.name = 'test-project'
        """
		when:
			var buildResult = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withPluginClasspath()
				.withDebug(true)
				.withArguments('help')
				.build()
		then:
			buildResult.task(':help').outcome == TaskOutcome.SUCCESS
	}
}
