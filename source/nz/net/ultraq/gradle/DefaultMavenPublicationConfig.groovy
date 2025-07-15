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
import nz.net.ultraq.gradle.fluent.MavenPublicationConfig

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.plugins.signing.SigningExtension

import groovy.transform.PackageScope

/**
 * Implementation for configuring a Maven publication.
 *
 * @author Emanuel Rabina
 */
@PackageScope
class DefaultMavenPublicationConfig implements MavenPublicationConfig, MavenPomConfig, MavenCentralConfig {

	private final Project project
	private final PublishingExtension publishing
	private final MavenPublication publication

	DefaultMavenPublicationConfig(Project project) {

		this.project = project
		project.pluginManager.apply('maven-publish')
		publishing = project.extensions.getByType(PublishingExtension)
		publication = publishing.publications.create('main', MavenPublication)
	}

	@Override
	MavenPublicationConfig addGroovydocJar() {

		if (!project.pluginManager.hasPlugin('groovy')) {
			throw new IllegalStateException(
				'Cannot add groovydocJar task on a non-Groovy project.  Be sure to ' +
				'add the groovy plugin first, or to have configured a groovy project ' +
				'using createGroovyProject().'
			)
		}

		var groovydocJar = project.tasks.register('groovydocJar', Jar) { groovydocJar ->
			groovydocJar.description = 'Assembles a jar archive containing the main groovydoc.'
			groovydocJar.group = 'build'
			groovydocJar.dependsOn('groovydoc')
			groovydocJar.from(project.tasks.named('groovydoc', Groovydoc).get().destinationDir)
			groovydocJar.destinationDirectory.set(project.layout.buildDirectory.dir('libs'))
			groovydocJar.archiveClassifier.set('javadoc')
		}
		project.tasks.named('assemble') { assembleTask ->
			assembleTask.dependsOn(groovydocJar.get())
		}
		publication.artifact(groovydocJar.get()) { artifact ->
			artifact.classifier = 'javadoc'
		}

		return this
	}

	@Override
	MavenPublicationConfig addJar(@DelegatesTo(Jar) Closure configure = null) {

		publication.from(project.components.named('java', SoftwareComponent).get())
		if (configure) {
			project.tasks.named('jar', Jar, configure)
		}
		return this
	}

	@Override
	MavenPublicationConfig addSourcesJar() {

		project.extensions.configure(JavaPluginExtension) { java ->
			java.withSourcesJar()
			project.tasks.named('sourcesJar', Jar) { jar ->
				jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			}
		}
		return this
	}

	@Override
	MavenPomConfig configurePom(@DelegatesTo(MavenPom) Closure configure = null) {

		publication.pom { pom ->
			pom.name.set(project.name)
			pom.description.set(project.description)
			if (configure) {
				configure.delegate = pom
				configure()
			}
		}
		return this
	}

	@Override
	MavenPomConfig useApache20License() {

		publication.pom { pom ->
			pom.licenses { licences ->
				licences.license { license ->
					license.name.set('The Apache Software License, Version 2.0')
					license.url.set('https://www.apache.org/licenses/LICENSE-2.0.txt')
					license.distribution.set('repo')
				}
			}
		}
		return this
	}

	@Override
	MavenPomConfig withDevelopers(Map<String, String>... developers) {

		publication.pom { pom ->
			pom.developers { pomDeveloperSpec ->
				developers.each { developer ->
					pomDeveloperSpec.developer { pomDeveloper ->
						pomDeveloper.name.set(developer.name)
						pomDeveloper.email.set(developer.email)
						pomDeveloper.url.set(developer.url)
					}
				}
			}
		}
		return this
	}

	@Override
	MavenPomConfig withGitHubScm(String owner, String repository = project.name) {

		publication.pom { pom ->
			pom.scm { scm ->
				scm.connection.set("scm:git:git@github.com:${owner}/${repository}.git")
				scm.developerConnection.set("scm:git:git@github.com:${owner}/${repository}.git")
				scm.url.set("https://github.com/${owner}/${repository}")
			}
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
}
