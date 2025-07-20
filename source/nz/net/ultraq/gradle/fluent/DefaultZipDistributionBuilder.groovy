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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.Distribution
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Groovydoc

import groovy.transform.CompileStatic
import javax.inject.Inject

/**
 * Implementation for configuring a ZIP distribution.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class DefaultZipDistributionBuilder implements ZipDistributionBuilder {

	private final Project project
	private final Distribution mainDistribution

	@Inject
	DefaultZipDistributionBuilder(Project project) {

		this.project = project

		project.pluginManager.apply('distribution')
		var distributionContainer = project.extensions.getByType(DistributionContainer).named('main') { main ->
			main.contents { spec ->
				project.pluginManager.withPlugin('java') {
					spec.from(project.tasks.named('jar', Jar).get().outputs.files)
				}
				spec.from(project.rootDir) {
					include('CHANGELOG.md')
					include('LICENSE.txt')
					include('README.md')
				}
			}
		}
		mainDistribution = distributionContainer.get()
		project.tasks.named('distTar', Task).configure { distTar ->
			distTar.enabled = false
		}
		project.tasks.named('distZip', Zip).configure { distZip ->
			if (project.pluginManager.hasPlugin('groovy')) {
				distZip.dependsOn('groovydoc')
			}
			distZip.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}
	}

	@Override
	ZipDistributionBuilder withDependenciesIn(String directory) {

		mainDistribution.contents { spec ->
			spec.from(project.configurations.named('runtimeClasspath').get()) {
				into(directory)
			}
		}
		return this
	}

	@Override
	ZipDistributionBuilder withGroovydocsIn(String directory) {

		mainDistribution.contents { spec ->
			spec.from(project.tasks.named('groovydoc', Groovydoc).get()) {
				into(directory)
			}
		}
		return this
	}

	@Override
	ZipDistributionBuilder withSourcesIn(String directory) {

		mainDistribution.contents { spec ->
			var mainSourceSet = project.extensions.getByType(SourceSetContainer).named('main').get()
			spec.from(mainSourceSet.extensions.getByType(GroovySourceDirectorySet).srcDirs) {
				into(directory)
			}
		}
		return this
	}
}
