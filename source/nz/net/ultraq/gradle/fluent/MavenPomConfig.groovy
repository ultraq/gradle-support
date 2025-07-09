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

/**
 * Configuration interface for quickly assembling Maven POM files.
 *
 * @author Emanuel Rabina
 */
interface MavenPomConfig extends MavenCentralEntry {

	/**
	 * Automatically fill in the {@code <licences>} section to have a license of
	 * the Apache 2.0 license.
	 */
	MavenPomConfig useApache20License()

	/**
	 * Set the {@code <developers>} section with the given developers.  The map
	 * properties accepted are {@code name}, {@code email}, and {@code ur}.
	 */
	MavenPomConfig withDevelopers(List<Map<String,String>> developers)

	/**
	 * Automatically fill in the {@code <scm>} section to reference a GitHub
	 * project.  The repository will default to the project name.
	 */
	MavenPomConfig withGitHubScm(String owner)

	/**
	 * Automatically fill in the {@code <scm>} section to reference a GitHub
	 * project.
	 */
	MavenPomConfig withGitHubScm(String owner, String repository)
}
