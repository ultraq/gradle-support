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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom

/**
 * Adds a {@code configurePom} method to the Gradle build script that can be
 * used to configure a Maven POM more succintly.
 *
 * <pre>
 * // build.gradle
 * plugins {
 *   id 'nz.net.ultraq.gradle.configure-pom' version 'x.y.z'
 * }
 *
 * publishing {
 *   publications {
 *      main(MavenPublication) {
 *        configurePom(pom) {
 *         inceptionYear = '2025'
 *         // All the usual MavenPom configuration can go here, but there are some
 *         // methods below to save a whole lot of typing
 *       }
 *         .useApache20License()
 *         .withGitHubScm('github-user', 'github-repository')
 *         .withDevelopers([
 *           name: 'My Name',
 *           email: 'me@example.org',
 *           url: 'https://example.org'
 *         ])
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Emanuel Rabina
 */
class ConfigurePomPlugin implements Plugin<Project> {

	private Project project

	@Override
	void apply(Project project) {

		this.project = project
		project.ext.configurePom = this::configurePom
	}

	/**
	 * Starts a fluent chain to configure the Maven POM.  The Gradle project
	 * {@code name} and {@code description} properties will also be used for their
	 * respective POM elements.
	 */
	MavenPomConfig configurePom(MavenPom pom, @DelegatesTo(MavenPom) Closure configure = null) {

		return new DefaultMavenPomConfig(project, pom, configure)
	}
}
