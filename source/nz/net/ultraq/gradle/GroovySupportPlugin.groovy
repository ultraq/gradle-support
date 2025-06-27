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
 * A plugin that works in tandem with Gradle's built-in {@code groovy} plugin to
 * help Groovy projects achieve configuration parity with Java projects.
 * Mainly, by allowing Groovy outputs/artifacts to participate in all the usual
 * lifecycle tasks.
 * <p>
 * <strong>For Groovydocs:</strong> currently, the {@code groovydoc} task is
 * totally separated from the rest of the Gradle lifecycle, making it hard to
 * add to other tasks which expect a {@code javadoc} artifact like pushing
 * documentation to Maven Central or building a distributable that includes the
 * documentation.  This plugin can be used to modify tasks to insert groovydocs
 * into the build process, including the option to replace the {@code javadoc}
 * artifact with it.
 *
 * @author Emanuel Rabina
 */
class GroovySupportPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.pluginManager.withPlugin('groovy') {
			project.extensions.create('groovy', GroovyExtension, project)
		}
	}
}
