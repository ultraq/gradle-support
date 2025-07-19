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

import org.gradle.api.Action
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

/**
 * For any part of the API that can begin the configuration to publish to Maven
 * Central.
 *
 * @author Emanuel Rabina
 */
interface MavenCentralBuilderEntry {

	/**
	 * Publish to any Maven repository of your configuration.
	 */
	MavenCentralBuilder publishTo(Action<? extends MavenArtifactRepository> closure)

	/**
	 * <p>Configure Maven Central publishing.  This will set up both the Maven
	 * Central and Snapshot repositories (pushing to snapshots if the project
	 * version ends with {@code -SNAPSHOT}), and apply the {@code signing} plugin.
	 *
	 * <p>Note that this is currently using the transitional Portal OSSRH Staging
	 * API that Sonatype has created to allow people to slowly migrate to their
	 * newer Publisher API.  This will be rewritten to utilize the Publisher API
	 * in future
	 *
	 * @param username
	 *   The username part of the user token generated from your Maven Central
	 *   account for the Publisher API.  DO NOT enter your actual credentials
	 *   here, instead reference a Gradle property or environment variable.
	 * @param password
	 *   The password part of the user token generated from your Maven Central
	 *   account for the Publisher API.  DO NOT enter your actual credentials
	 *   here, instead reference a Gradle property or environment variable.
	 */
	MavenCentralBuilder publishToMavenCentral(String username, String password)
}
