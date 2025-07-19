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
 * Configuration interface for a ZIP archive of sources, binaries, and
 * accompanying documentation for both consumers and developers.
 *
 * @author Emanuel Rabina
 */
interface ZipDistributionBuilder {

	/**
	 * Include runtime dependencies of the main JAR and place them in the given
	 * directory.
	 */
	ZipDistributionBuilder withDependenciesIn(String directory)

	/**
	 * Include groovydocs and place them in the given directory.
	 */
	ZipDistributionBuilder withGroovydocsIn(String directory)

	/**
	 * Include source files and place them in the given directory.
	 */
	ZipDistributionBuilder withSourcesIn(String directory)
}
