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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

/**
 * Tests for the {@link GroovySupportPlugin} and the {@code groovydoc { ... }}
 * script block that it applies.
 *
 * @author Emanuel Rabina
 */
class GroovySupportPluginTests extends Specification {

	@TempDir
	File testProjectDir
	File settingsFile
	File buildFile

	def setup() {
		settingsFile = new File(testProjectDir, 'settings.gradle')
		settingsFile << "rootProject.name = 'test-project'"

		buildFile = new File(testProjectDir, 'build.gradle')
	}

	def "Does nothing with an empty project"() {
		given:
			buildFile << """
				plugins {
          id 'nz.net.ultraq.gradle.groovy-support'
        }
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

	def "Adds a groovydoc script block to add a groovydocJar task"() {
		given:
			buildFile << """
				plugins {
				  id 'groovy'
          id 'nz.net.ultraq.gradle.groovy-support'
        }
        groovy {
          withGroovydocJar() {
            replaceJavadoc = true
          }
        }
        """
		when:
			var buildResult = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withPluginClasspath()
				.withDebug(true)
				.withArguments('assemble')
				.build()
		then:
			buildResult.task(':groovydocJar').outcome == TaskOutcome.SUCCESS
	}
}
