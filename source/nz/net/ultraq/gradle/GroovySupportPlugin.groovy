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
 * Works in tandem with Gradle's built-in {@code groovy} plugin to help Groovy
 * projects achieve configuration parity with Java projects.  Mainly, by
 * allowing Groovy outputs/artifacts to participate in all the usual lifecycle
 * tasks.
 *
 * <pre>
 * // build.gradle
 * plugins {
 *   id 'nz.net.ultraq.gradle.groovy-support' version 'x.y.z'
 * }
 *
 * groovy {
 *   withGroovydocJar() {
 *     replaceJavadoc = true
 *   }
 * }
 * </pre>
 *
 * <p>This plugin adds a {@code groovy} script block which can be used for
 * configuration.
 *
 * <p>The {@code withGroovydocJar()} method is similar to Gradle's
 * {@code withJavadocJar()} in that it adds a {@code groovydocJar} task to the
 * project, and will also ensure that task will be run when the {@code assemble}
 * lifecycle tasks is used.
 *
 * <p>An optional configuration closure can be supplied to further configure the
 * task, though the only option right now is {@code replaceJavadoc} which will
 * make the JAR use the `javadoc` classifier so it can stand in place of the
 * javadoc JAR.  This is especially useful for things that rely on the presence
 * of the javadoc JAR for documentation, eg: Maven Central so that 'Download
 * documentation' options of IDEs can work, or services like <a href="https://javadoc.io">javadoc.io</a>
 * which use the JAR as the source for documentation.
 *
 * @author Emanuel Rabina
 */
class GroovySupportPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.pluginManager.withPlugin('groovy') {
			project.extensions.create('groovy', GroovySupportExtension, project)
		}
	}
}
