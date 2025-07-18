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

import nz.net.ultraq.gradle.fluent.MavenPomConfig

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests for configuring a Maven POM.
 *
 * @author Emanuel Rabina
 */
class DefaultMavenPomConfigTests extends Specification {

	Project project
	MavenPomConfig config

	def setup() {
		project = ProjectBuilder.builder().build()
		project.pluginManager.apply('nz.net.ultraq.gradle.configure-pom')
		project.pluginManager.apply('maven-publish')
		var publishing = project.extensions.getByName('publishing') as PublishingExtension
		var mainPublication = publishing.publications.create('main', MavenPublication)
		config = new DefaultMavenPomConfig(project, mainPublication.pom)
	}

	def "Fills in an Apache 2.0 License"() {
		when:
			config.useApache20License()
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var mainPublication = publishing.publications.named('main').get() as MavenPublication
			mainPublication.pom.licenses.size() == 1
			verifyAll(mainPublication.pom.licenses.first()) {
				name.get() == 'The Apache Software License, Version 2.0'
				url.get() == 'https://www.apache.org/licenses/LICENSE-2.0.txt'
				distribution.get() == 'repo'
			}
	}

	def "Fills in GitHub SCM details"() {
		when:
			config.withGitHubScm('ultraq', 'gradle-support')
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var publication = publishing.publications.named('main').get() as MavenPublication
			verifyAll(publication.pom.scm) {
				connection.get() == 'scm:git:git@github.com:ultraq/gradle-support.git'
				developerConnection.get() == 'scm:git:git@github.com:ultraq/gradle-support.git'
				url.get() == 'https://github.com/ultraq/gradle-support'
			}
	}

	def "Adds developer details"() {
		when:
			config.withDevelopers([
				name: 'Emanuel Rabina',
				email: 'emanuelrabina@gmail.com',
				url: 'https://www.ultraq.net.nz'
			])
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var mainPublication = publishing.publications.named('main').get() as MavenPublication
			mainPublication.pom.developers.size() == 1
			verifyAll(mainPublication.pom.developers.first()) {
				name.get() == 'Emanuel Rabina'
				email.get() == 'emanuelrabina@gmail.com'
				url.get() == 'https://www.ultraq.net.nz'
			}
	}
}
