/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.gradle.fluent

import nz.net.ultraq.gradle.FluentConfigurationPlugin
import nz.net.ultraq.gradle.FluentConfigurationPluginExtension

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests for configuring a Maven Central upload bundle.
 *
 * @author Emanuel Rabina
 */
class MavenCentralPublisherBundleBuilderTests extends Specification {

	Project project
	FluentConfigurationPluginExtension configure
	MavenCentralPublisherBundleBuilder builder

	def setup() {

		project = ProjectBuilder.builder().build()
		project.pluginManager.apply(FluentConfigurationPlugin)
		configure = project.extensions.getByName('configure') as FluentConfigurationPluginExtension
		builder = configure.createMavenCentralPublisherBundle()
	}

	def 'Specified projects must have the maven-publish plugin applied'() {
		when:
			builder.forProjects(project)
		then:
			thrown(AssertionError)
	}

	def 'This project must have the maven-publish plugin applied'() {
		when:
			builder.forThisProject()
		then:
			thrown(AssertionError)
	}

	def 'Configures snapshot repository for snapshot releases'() {
		when:
			project.plugins.apply('maven-publish')
			project.version = '0.1.0-SNAPSHOT'
			builder
				.forThisProject()
				.withCredentials('username', 'password')
		then:
			var publishing = project.extensions.getByType(PublishingExtension)
			publishing.repositories.find { repository ->
				return repository instanceof MavenArtifactRepository &&
					repository.url == 'https://central.sonatype.com/repository/maven-snapshots/'.toURI()
			} != null
	}

	def 'Configures signing and upload tasks for non-snapshot releases'() {
		when:
			project.plugins.apply('java-library')
			project.plugins.apply('maven-publish')
			project.version = '0.1.0'
			configure.createMavenPublication()
			builder
				.forThisProject()
				.withCredentials('username', 'password')
		then:
			project.plugins.hasPlugin('signing')
			var publishing = project.extensions.getByType(PublishingExtension)
			publishing.repositories.find { it instanceof MavenArtifactRepository } != null
//			repository.url == project.layout.buildDirectory.dir('staging-deploy')
			project.tasks.getByName('createUploadBundle') != null
			project.tasks.getByName('publishAsUploadBundle') != null
			var publishTask = project.tasks.getByName('publish')
			publishTask.finalizedBy('publishAsUploadBundle')
	}
}
