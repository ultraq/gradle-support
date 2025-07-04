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
 * Adds a {@code configure} script block to a {@code build.gradle} file, within
 * which a fluent API can be used to configure a project.
 *
 * @author Emanuel Rabina
 */
class FluentConfigurationPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.extensions.create('configure', FluentConfigurationPluginExtension, project)
	}
}
