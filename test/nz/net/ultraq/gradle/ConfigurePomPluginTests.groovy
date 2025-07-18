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

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests for the {@code configure-pom} plugin.
 *
 * @author Emanuel Rabina
 */
class ConfigurePomPluginTests extends Specification {

	def "Adds a configurePom method to the project"() {
		given:
			var project = ProjectBuilder.builder().build()
		when:
			project.pluginManager.apply('nz.net.ultraq.gradle.configure-pom')
		then:
			project.hasProperty('configurePom')
	}

	def "Calling configurePom sets up the POM with some initial values and the configure closure"() {
		given:
			var project = ProjectBuilder.builder()
				.withName('My project')
				.build()
			project.pluginManager.apply('nz.net.ultraq.gradle.configure-pom')
			project.pluginManager.apply('maven-publish')
		when:
			project.description = 'Test project description'
			project.extensions.configure('publishing') { PublishingExtension publishing ->
				var mainPublication = publishing.publications.create('main', MavenPublication)
				project.configurePom(mainPublication.pom) {
					inceptionYear = '2025'
				}
			}
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var mainPublication = publishing.publications.named('main').get() as MavenPublication
			var pom = mainPublication.pom
			pom.name.get() == project.name
			pom.description.get() == project.description
			pom.inceptionYear.get() == '2025'
	}
}
