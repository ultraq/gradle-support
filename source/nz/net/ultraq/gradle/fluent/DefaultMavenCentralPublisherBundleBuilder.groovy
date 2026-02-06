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

import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.FileBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Zip
import org.gradle.plugins.signing.SigningExtension

import groovy.transform.CompileStatic
import javax.inject.Inject

/**
 * Implementation for configuring a bundled upload to the Maven Central
 * Publisher API.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class DefaultMavenCentralPublisherBundleBuilder implements MavenCentralPublisherBundleBuilder {

	private final Project project
	private List<Project> projectsForPublishing
	private boolean automaticPublishing = false

	@Inject
	DefaultMavenCentralPublisherBundleBuilder(Project project) {

		this.project = project
		project.plugins.apply('base')
	}

	@Override
	MavenCentralPublisherBundleBuilder forProjects(Project... projects) {

		projects.each { project ->
			assert project.plugins.hasPlugin('maven-publish') :
				"Project ${project.name} does not have a Maven publication configured"
		}
		projectsForPublishing = projects.toList()
		return this
	}

	@Override
	MavenCentralPublisherBundleBuilder forThisProject() {

		return forProjects(project)
	}

	@Override
	MavenCentralPublisherBundleBuilder useAutomaticPublishing() {

		automaticPublishing = true
		return this
	}

	@Override
	MavenCentralPublisherBundleBuilder withCredentials(String username, String password) {

		if (project.version.toString().endsWith('SNAPSHOT')) {
			withCredentialsForSnapshotReleases(username, password)
		}
		else {
			withCredentialsForFinalReleases(username, password)
		}
		return this
	}

	/**
	 * New code path for publishing a single bundle with the Maven Central
	 * Publisher API.
	 */
	private void withCredentialsForFinalReleases(String username, String password) {

		var stagingDirectory = project.layout.buildDirectory.dir('staging-deploy')
		var bundleDirectory = project.layout.buildDirectory.dir('staging-bundle')

		projectsForPublishing.each { project ->
			var publishing = project.extensions.getByType(PublishingExtension)
			project.pluginManager.apply('signing')
			project.extensions.configure(SigningExtension) { signing ->
				signing.sign(publishing.publications.named('main', MavenPublication).get())
			}
			publishing.repositories { repositories ->
				repositories.maven { maven ->
					maven.url = stagingDirectory
				}
			}
		}

		project.tasks.register('createUploadBundle', Zip) { zip ->
			zip.group = 'publishing'
			zip.dependsOn(projectsForPublishing.collect { project -> project.tasks.named('publish') })
			zip.from(stagingDirectory)
			zip.destinationDirectory.set(bundleDirectory)
			zip.archiveBaseName.set(project.name)
		}

		project.tasks.register('publishAsUploadBundle') { task ->
			task.group = 'publishing'
			task.dependsOn('createUploadBundle')
			var bundle = bundleDirectory.get().file("${project.name}-${project.version}.zip").getAsFile()
			task.doLast {
				var deploymentId = HttpClients.createDefault().withCloseable { httpClient ->
					var post = new HttpPost('https://central.sonatype.com/api/v1/publisher/upload' +
						"?publishingType=${automaticPublishing ? 'AUTOMATIC' : 'USER_MANAGED'}")
					post.setHeader('Authorization', "Bearer ${Base64.getEncoder().encodeToString("${username}:${password}".getBytes())}")
					post.setEntity(MultipartEntityBuilder.create()
						.addPart('bundle', new FileBody(bundle, ContentType.APPLICATION_OCTET_STREAM))
						.build())
					return httpClient.execute(post) { response ->
						var responseCode = response.code
						if (responseCode < 200 || responseCode >= 300) {
							throw new Exception("Failed to publish bundle, received response code of ${responseCode}")
						}
						return response.entity.content.text
					}
				}
				println "Bundle published, deployment ID is ${deploymentId}"
			}
		}

		project.tasks.named('clean', Delete) { clean ->
			clean.doLast {
				clean.delete(stagingDirectory)
				clean.delete(bundleDirectory)
			}
		}

		project.tasks.named('publish') { publish ->
			publish.finalizedBy('publishAsUploadBundle')
		}
	}

	/**
	 * Continue to use Gradle's built-in Maven Central publisher for snapshot
	 * releases.
	 */
	private void withCredentialsForSnapshotReleases(String username, String password) {

		projectsForPublishing.each { project ->
			project.extensions.getByType(PublishingExtension).repositories { repositories ->
				repositories.maven { maven ->
					maven.url = 'https://central.sonatype.com/repository/maven-snapshots/'
					maven.credentials { credentials ->
						credentials.username = username
						credentials.password = password
					}
				}
			}
		}
	}
}
