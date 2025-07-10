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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings

/**
 * Adds the Maven Central and Snapshots repositories to a single project, or all
 * projects if added to a settings file.
 *
 * <pre>
 * // settings.gradle or build.gradle
 * plugins {
 *   id 'nz.net.ultraq.gradle.use-maven-central-repositories' version 'x.y.z'
 * }
 * </pre>
 *
 * @author Emanuel Rabina
 */
class UseMavenCentralRepositoriesPlugin implements Plugin {

	@Override
	void apply(Object target) {

		// Because of generic type erasure, I have to dispatch to a Settings or Project handler myself...
		switch (target) {
			case Settings -> applyMavenCentralRepositories(target.dependencyResolutionManagement.repositories)
			case Project -> applyMavenCentralRepositories(target.repositories)
			default -> throw new IllegalArgumentException("${target.class} is not supported for this plugin")
		}
	}

	private static void applyMavenCentralRepositories(RepositoryHandler repositories) {

		repositories.mavenCentral()
		repositories.maven {
			name = 'Maven Central Snapshots'
			url = 'https://central.sonatype.com/repository/maven-snapshots/'
		}
	}
}
