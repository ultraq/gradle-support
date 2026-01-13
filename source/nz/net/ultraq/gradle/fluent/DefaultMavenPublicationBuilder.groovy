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

package nz.net.ultraq.gradle.fluent

import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.FileBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.plugins.signing.SigningExtension

import groovy.transform.CompileStatic
import javax.inject.Inject

/**
 * Implementation for configuring a Maven publication.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class DefaultMavenPublicationBuilder implements MavenPublicationBuilder, MavenPomBuilder, MavenCentralBuilder {

	private final Project project
	private final PublishingExtension publishing
	private final MavenPublication publication

	@Inject
	DefaultMavenPublicationBuilder(Project project) {

		this.project = project
		project.pluginManager.apply('maven-publish')
		publishing = project.extensions.getByType(PublishingExtension)
		publication = publishing.publications.create('main', MavenPublication)
		var javaComponent = project.components.findByName('java')
		if (javaComponent) {
			publication.from(javaComponent)
		}
	}

	@Override
	MavenPomBuilder configurePom(Action<? extends MavenPom> configure = null) {

		publication.pom { pom ->
			pom.name.set(project.name)
			pom.description.set(project.description)
			configure?.execute(pom)
		}
		return this
	}

	@Override
	MavenCentralBuilder publishTo(Action<? extends MavenArtifactRepository> configure) {

		publishing.repositories { repositories ->
			repositories.maven(configure)
		}
		return this
	}

	@Override
	MavenCentralBuilder publishToMavenCentral(String username, String password) {

		var isSnapshot = project.version.toString().endsWith('SNAPSHOT')

		// For snapshot releases, use Gradle's built-in Maven Central publisher
		if (isSnapshot) {
			publishing.repositories { repositories ->
				repositories.maven { maven ->
					maven.url = 'https://central.sonatype.com/repository/maven-snapshots/'
					maven.credentials { credentials ->
						credentials.username = username
						credentials.password = password
					}
				}
			}
		}

		// For full releases, create an upload bundle to submit to the new publisher API
		else {
			project.pluginManager.apply('signing')
			project.extensions.configure(SigningExtension) { signing ->
				signing.sign(publication)
			}

			publishing.repositories { repositories ->
				repositories.maven { maven ->
					maven.url = project.layout.buildDirectory.dir('staging-deploy')
				}
			}

			project.tasks.register('createStagingBundle', Zip) { zip ->
				zip.group = 'publishing'
				zip.dependsOn('publish')
				zip.from(project.layout.buildDirectory.dir('staging-deploy'))
				zip.destinationDirectory.set(project.layout.buildDirectory.dir('staging-bundle'))
				zip.archiveBaseName.set(project.name)
			}

			project.tasks.register('publishAsUploadBundle') { task ->
				task.group = 'publishing'
				task.dependsOn('createStagingBundle')
				var bundle = project.layout.buildDirectory.file("staging-bundle/${project.name}-${project.version}.zip").get().getAsFile()
				task.doLast {
					var deploymentId = HttpClients.createDefault().withCloseable { httpClient ->
						var post = new HttpPost('https://central.sonatype.com/api/v1/publisher/upload')
						post.setHeader('Authorization', "Bearer ${Base64.getEncoder().encodeToString("${username}:${password}".getBytes())}")
						post.setEntity(MultipartEntityBuilder.create()
							.addPart('bundle', new FileBody(bundle, ContentType.APPLICATION_OCTET_STREAM))
							.build())
						return httpClient.execute(post) { response ->
							var responseCode = response.code
							if (responseCode < 200 || responseCode >= 300) {
								throw new Exception("Failed to publish bundle, received response code of ${response.getCode()}")
							}
							return response.entity.content.text
						}
					}
					println "Deployment ID: ${deploymentId}"
				}
			}

			project.tasks.named('clean', Delete) { clean ->
				clean.delete(project.layout.buildDirectory.dir('staging-deploy'))
				clean.delete(project.layout.buildDirectory.dir('staging-bundle'))
			}

			project.tasks.named('publish').configure { task ->
				task.finalizedBy('publishAsUploadBundle')
			}
		}

		return this
	}

	@Override
	MavenPomBuilder useApache20License() {

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
	MavenPublicationBuilder withArtifacts(Object... sources) {

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

	@Override
	MavenPomBuilder withDevelopers(Map<String, String>... developers) {

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
	MavenPomBuilder withGitHubScm(String owner, String repository = project.rootProject.name) {

		publication.pom { pom ->
			pom.scm { scm ->
				scm.connection.set("scm:git:git@github.com:${owner}/${repository}.git".toString())
				scm.developerConnection.set("scm:git:git@github.com:${owner}/${repository}.git".toString())
				scm.url.set("https://github.com/${owner}/${repository}".toString())
			}
		}
		return this
	}
}
