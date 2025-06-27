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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.jvm.tasks.Jar

/**
 * The {@code groovydoc { ... }} script block added to build scripts when the
 * {@link GroovydocPlugin} is applied.
 *
 * @author Emanuel Rabina
 */
abstract class GroovydocExtension {

	private final Project project

	GroovydocExtension(Project project) {

		this.project = project
	}

	@Nested
	abstract GroovydocJarOptions getGroovydocJarOptions()

	/**
	 * Add a {@code groovydocJar} task to the task lifecycle that is run as part
	 * of {@code assemble}.
	 *
	 * @param configure
	 *   Configure {@code groovydocJar} options.
	 */
	void withGroovydocJar(Action<GroovydocJarOptions> configure = null) {

		var groovydocJarTask = project.tasks.register('groovydocJar', Jar) { groovydocJar ->
			groovydocJar.description = 'Assembles a jar archive containing the main groovydoc.'
			groovydocJar.group = 'build'
			groovydocJar.from(project.tasks.named('groovydoc', Groovydoc).map { it -> it.destinationDir })
			groovydocJar.destinationDirectory.set(project.file('build/libs'))
			groovydocJar.archiveClassifier.set('groovydoc')

			if (configure) {
				var options = getGroovydocJarOptions()
				configure.execute(options)

				if (options.replaceJavadoc.get()) {
					groovydocJar.archiveClassifier.set('javadoc')
				}
			}
		}
		project.tasks.named('assemble').configure { assembleTask ->
			assembleTask.dependsOn(groovydocJarTask.get())
		}
	}

	/**
	 * Options to apply to the {@code groovydocJar} task.
	 */
	static interface GroovydocJarOptions {

		Property<Boolean> getReplaceJavadoc()
	}
}
