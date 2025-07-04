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
 * Configuration interface for testing.
 *
 * <p>Note that this works over the `jvm-test-suite` plugin from Gradle, which
 * is still incubating.
 *
 * @author Emanuel Rabina
 */
interface TestingConfig {

	/**
	 * Configure all test suites to use JUnit Jupiter.
	 */
	TestingConfig useJUnitJupiter()

	/**
	 * Configure the testing dependencies for the project.
	 */
	TestingConfig withTestDependencies(@DelegatesTo(DependencyHandler) Closure configure)

	/**
	 * Set the directory in which test code and assets will reside.
	 */
	TestingConfig withTestDirectory(Object path)
}
