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

import nz.net.ultraq.gradle.fluent.MavenCentralConfig
import nz.net.ultraq.gradle.fluent.MavenPomConfig
import nz.net.ultraq.gradle.fluent.MavenPomConfigChain
import nz.net.ultraq.gradle.fluent.MavenPublicationConfig

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.signing.SigningExtension

import groovy.transform.PackageScope

/**
 * Implementation for configuring a Maven publication.
 *
 * @author Emanuel Rabina
 */
@PackageScope
class DefaultMavenPublicationConfig implements MavenPublicationConfig, MavenPomConfigChain, MavenCentralConfig {

	private final Project project
	private final PublishingExtension publishing
	private final MavenPublication publication
	@Delegate
	private MavenPomConfig mavenPomConfig

	DefaultMavenPublicationConfig(Project project) {

		this.project = project
		project.pluginManager.apply('maven-publish')
		publishing = project.extensions.getByType(PublishingExtension)
		publication = publishing.publications.create('main', MavenPublication)
	}

	@Override
	MavenPomConfig configurePom(@DelegatesTo(MavenPom) Closure configure = null) {

		publication.pom { pom ->
			mavenPomConfig = new DefaultMavenPomConfig(project, pom, configure)
		}
		return this
	}

	@Override
	MavenCentralConfig publishTo(@DelegatesTo(MavenArtifactRepository) Closure configure) {

		publishing.repositories { repositories ->
			repositories.maven(configure)
		}
		return this
	}

	@Override
	MavenCentralConfig publishToMavenCentral(String username, String password) {

		project.pluginManager.apply('signing')
		project.extensions.configure(SigningExtension) { signing ->
			signing.sign(publication)
		}
		publishing.repositories { repositories ->
			repositories.maven { maven ->
				maven.url = project.version.endsWith('SNAPSHOT') ?
					'https://central.sonatype.com/repository/maven-snapshots/' :
					'https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/'
				maven.credentials { credentials ->
					credentials.username = username
					credentials.password = password
				}
			}
		}
		return this
	}

	@Override
	MavenPublicationConfig withArtifacts(Object... sources) {

		sources.each { source ->
			if (source instanceof Jar && source.name == 'groovydocJar') {
				publication.artifact(source) { artifact ->
					artifact.classifier = 'javadoc'
				}
			}
			else {
				publication.artifact(source)
			}
		}
		return this
	}
}
