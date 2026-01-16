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

import org.gradle.api.Project

/**
 * Configuration interface for taking several projects (eg: subprojects in a
 * multi-project build, each of which create their own artifact on Maven Central
 * under the same namespace and have dependencies on each other) and publishing
 * them as a single upload bundle via the Maven Central Publisher API.  This is
 * so that validation checks which ensure that dependencies exist, will pass.
 *
 * <p>All of this will only apply if the release being made is not a snapshot -
 * snapshots do not undergo validation, and so can continue to be published to
 * the snapshots API.
 *
 * @author Emanuel Rabina
 */
interface MavenCentralPublisherBundleBuilder {

	/**
	 * Select which projects to include in the upload bundle.  Each included
	 * project should have a Maven publication already configured so that this
	 * task can make sure the publication is signed and configured to deploy to a
	 * staging directory that will live at `build/staging-deploy`.
	 */
	MavenCentralPublisherBundleBuilder forProjects(Project... projects)

	/**
	 * Convenience method to apply the current project as the only one with
	 * artifacts for publishing.
	 */
	MavenCentralPublisherBundleBuilder forThisProject()

	/**
	 * Specify the username/password for publishing the staged artifacts to Maven
	 * Central.
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
	void withCredentials(String username, String password)
}
