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

/**
 * A plugin for allowing groovydocs to participate in other Gradle tasks where
 * javadocs are normally expected.
 * <p>
 * Currently, the {@code groovydoc} task is totally separated from the rest of
 * the Gradle lifecycle, making it hard to add to other tasks which expect a
 * {@code javadoc} artifact (eg: pushing documentation to Maven Central).  This
 * plugin will modify tasks to insert groovydocs into the build process,
 * including the option to replace the {@code javadoc} task with it.
 *
 * @author Emanuel Rabina
 */
class GroovydocPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.pluginManager.withPlugin('groovy') {
			project.extensions.create('groovydocSupport', GroovydocExtension, project)
		}
	}
}
