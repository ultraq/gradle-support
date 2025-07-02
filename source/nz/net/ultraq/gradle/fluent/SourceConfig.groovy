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
	GroovyProjectConfig expandExtensionModuleVersion()

	/**
	 * Expands the given property reference in the Groovy extension module
	 * manifest file to the given value.
	 */
	GroovyProjectConfig expandExtensionModuleVersion(String propertyName, String value)

	/**
	 * Start configuration of the source code, ie: {@code main} source set, by
	 * setting the directory in which source code and assets will reside.
	 */
	SourceConfig withSourceDirectory(Object path)
}
