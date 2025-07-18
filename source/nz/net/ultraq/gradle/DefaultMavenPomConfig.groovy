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
import org.gradle.api.publish.maven.MavenPom

import groovy.transform.PackageScope

/**
 * Implementation for configuring a Maven POM.
 *
 * @author Emanuel Rabina
 */
@PackageScope
class DefaultMavenPomConfig implements MavenPomConfig {

	private final Project project
	private final MavenPom pom

	DefaultMavenPomConfig(Project project, MavenPom pom, @DelegatesTo(MavenPom) Closure configure = null) {

		this.project = project
		this.pom = pom

		pom.name.set(project.name)
		pom.description.set(project.description)
		if (configure) {
			configure.delegate = pom
			configure()
		}
	}

	@Override
	MavenPomConfig useApache20License() {

		pom.licenses { licences ->
			licences.license { license ->
				license.name.set('The Apache Software License, Version 2.0')
				license.url.set('https://www.apache.org/licenses/LICENSE-2.0.txt')
				license.distribution.set('repo')
			}
		}
		return this
	}

	@Override
	MavenPomConfig withDevelopers(Map<String, String>... developers) {

		pom.developers { pomDeveloperSpec ->
			developers.each { developer ->
				pomDeveloperSpec.developer { pomDeveloper ->
					pomDeveloper.name.set(developer.name)
					pomDeveloper.email.set(developer.email)
					pomDeveloper.url.set(developer.url)
				}
			}
		}
		return this
	}

	@Override
	MavenPomConfig withGitHubScm(String owner, String repository = project.name) {

		pom.scm { scm ->
			scm.connection.set("scm:git:git@github.com:${owner}/${repository}.git")
			scm.developerConnection.set("scm:git:git@github.com:${owner}/${repository}.git")
			scm.url.set("https://github.com/${owner}/${repository}")
		}
		return this
	}
}
