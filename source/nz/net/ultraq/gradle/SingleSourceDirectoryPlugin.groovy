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
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar

/**
 * Make the source and resources directories be the same directory.  This is to
 * help those that prefer having source code and assets co-located so you're not
 * jumping around the file tree looking for related items.
 *
 * @author Emanuel Rabina
 */
class SingleSourceDirectoryPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		var sourceSetContainer = project.extensions.findByType(SourceSetContainer)
		if (sourceSetContainer) {
			sourceSetContainer.configureEach { sourceSet ->
				sourceSet.extensions.add('withSingleSourceDirectory', { Object path ->
					var sourceDirectorySets = [sourceSet.java, sourceSet.resources]
					sourceSet.extensions.extraProperties.properties.each { _name, property ->
						if (property instanceof SourceDirectorySet) {
							sourceDirectorySets << property
						}
					}
					sourceDirectorySets*.srcDirs = [project.file(path)]

					project.pluginManager.withPlugin('java') { javaPlugin ->
						sourceSet.resources.exclude('**/*.java')
					}
					project.pluginManager.withPlugin('groovy') { groovyPlugin ->
						sourceSet.resources.exclude('**/*.groovy')
					}
				})
			}

			if (project.tasks.names.contains('sourcesJar')) {
				project.tasks.named('sourcesJar', Jar).configure { jarTask ->
					jarTask.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
				}
			}
		}
	}
}
