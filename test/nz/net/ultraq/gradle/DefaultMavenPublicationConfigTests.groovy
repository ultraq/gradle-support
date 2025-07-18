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

import nz.net.ultraq.gradle.fluent.MavenPublicationConfig

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests for configuring a Maven publication.
 *
 * @author Emanuel Rabina
 */
class DefaultMavenPublicationConfigTests extends Specification {

	Project project
	MavenPublicationConfig config

	def setup() {
		project = ProjectBuilder.builder().build()
		config = new DefaultMavenPublicationConfig(project)
	}

	def "Adds artifacts"() {
		when:
			project.pluginManager.apply('groovy')
			var java = project.extensions.getByName('java') as JavaPluginExtension
			java.withSourcesJar()
			config.withArtifacts(project.tasks.named('sourcesJar').get())
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var mainPublication = publishing.publications.named('main').get() as MavenPublication
			mainPublication.artifacts.size() == 1
	}

	def "Adds the groovydocJar artifact with a javadoc classifier"() {
		when:
			project.pluginManager.apply('groovy')
			var groovydocJarTask = project.tasks.register('groovydocJar', Jar)
			config.withArtifacts(groovydocJarTask.get())
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var mainPublication = publishing.publications.named('main').get() as MavenPublication
			mainPublication.artifacts.size() == 1
			mainPublication.artifacts.first().classifier == 'javadoc'
	}

	def "POM configuration is just a wrapper for the configure POM plugin"() {
		when:
			config.configurePom() {
				inceptionYear = '2025'
			}
		then:
			var publication = project.extensions.getByType(PublishingExtension).publications.named('main').get() as MavenPublication
			publication.pom.inceptionYear.get() == '2025'
	}

	def "Publishes to any Maven repository"() {
		when:
			config.publishTo {
				name = 'My local repo'
				url = project.file('../my-local-repo')
			}
		then:
			var publishing = project.extensions.getByName('publishing') as PublishingExtension
			var localRepo = publishing.repositories.named('My local repo', MavenArtifactRepository).get()
			localRepo.url == project.file('../my-local-repo').toURI()
	}
}
