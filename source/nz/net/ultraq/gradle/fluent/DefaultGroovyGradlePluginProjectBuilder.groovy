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

import groovy.transform.CompileStatic
import javax.inject.Inject

/**
 * Implementation for configuring a Groovy Gradle plugin project.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class DefaultGroovyGradlePluginProjectBuilder extends DefaultGroovyProjectBuilder implements GroovyGradlePluginProjectBuilder {

	@Inject
	DefaultGroovyGradlePluginProjectBuilder(Project project) {

		super(project)
		project.pluginManager.apply('groovy-gradle-plugin')
	}

	@Override
	GroovyGradlePluginProjectBuilder useGradlePluginPortal() {

		project.repositories.gradlePluginPortal()
		return this
	}
}
