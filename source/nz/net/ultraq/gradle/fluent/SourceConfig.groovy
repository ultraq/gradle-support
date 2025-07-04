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

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Configuration interface for the project source code.
 *
 * @author Emanuel Rabina
 */
interface SourceConfig extends TestingEntry {

	/**
	 * Expands the {@code moduleVersion} property reference in the Groovy
	 * extension module manifest file to the Gradle project version.
	 */
	SourceConfig expandExtensionModuleVersion()

	/**
	 * Expands the given property reference in the Groovy extension module
	 * manifest file to the given value.
	 */
	SourceConfig expandExtensionModuleVersion(String propertyName, String value)

	/**
	 * Configure the dependencies for the project.
	 */
	SourceConfig withDependencies(@DelegatesTo(DependencyHandler) Closure configure)

	/**
	 * Set the directory in which source code and assets will reside.  This is for
	 * those who prefer co-locating source code and assets.
	 */
	SourceConfig withSourceDirectory(Object path)
}
