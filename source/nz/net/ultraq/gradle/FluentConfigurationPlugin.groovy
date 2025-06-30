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
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.base.TestingExtension

import groovy.transform.TupleConstructor

/**
 * Plugin for adding a fluent API within a {@code configure} script block.
 *
 * @author Emanuel Rabina
 */
class FluentConfigurationPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.extensions.create('configure', FluentConfigurationExtension, project)
	}

	/**
	 * The {@code configure} script block.
	 */
	@TupleConstructor(defaults = false)
	static abstract class FluentConfigurationExtension {

		final Project project

		private GroovyProjectConfig groovyProject

		/**
		 * Starts a fluent chain for configuring a Groovy project.
		 */
		GroovyProjectConfig groovyProject() {
			groovyProject = new GroovyProjectConfig()
			return groovyProject
		}

		/**
		 * Starts a fluent chain for configuring repositories.
		 */
		RepositoriesConfig repositories() {
			return new RepositoriesConfig()
		}

		/**
		 * Starts a fluent chain for configuring sourcesets.
		 */
		SourceSetsConfig sourceSets() {
			return new SourceSetsConfig()
		}

		/**
		 * Starts a fluent chain for configuring testing.
		 */
		TestingConfig testing() {
			return new TestingConfig()
		}

		/**
		 * Configuration interface for putting together a Groovy project in Gradle.
		 */
		@TupleConstructor(defaults = false)
		class GroovyProjectConfig {

			GroovyProjectConfig() {
				project.pluginManager.apply('groovy')
			}

			/**
			 * Sets the version of Java to use in the toolchain configuration.
			 */
			GroovyProjectConfig useJavaVersion(int version) {
				project.extensions.configure(JavaPluginExtension) { java ->
					java.toolchain.languageVersion.set(JavaLanguageVersion.of(version))
				}
				return this
			}
		}

		/**
		 * Configuration interface for sourcesets.
		 */
		@TupleConstructor(defaults = false)
		class SourceSetsConfig {

			/**
			 * Sets a single source directory for both source and resource files in
			 * the named sourceset.
			 */
			private SourceSetsConfig configureSourceDirectoryForSourceSet(Object path, String name) {
				project.pluginManager.withPlugin('java') {
					project.extensions.configure(SourceSetContainer) { sourceSets ->
						sourceSets.named(name) { sourceSet ->
							var sourceDirectorySets = [sourceSet.java, sourceSet.resources]
							if (project.pluginManager.hasPlugin('groovy')) {
								sourceDirectorySets << sourceSet.extensions.getByType(GroovySourceDirectorySet)
							}
							sourceDirectorySets*.srcDirs = [project.file(path)]
							sourceSet.resources.exclude('**/*.java', '**/*.groovy')
						}
					}
				}
				return this
			}

			/**
			 * Sets a single source directory for both source and resource files in
			 * the {@code main} sourceset.
			 */
			SourceSetsConfig withMainSourceDirectory(Object path) {
				return configureSourceDirectoryForSourceSet(path, 'main')
			}

			/**
			 * Sets a single source directory for both source and resource files in
			 * the {@code test} sourceset.
			 */
			SourceSetsConfig withTestSourceDirectory(Object path) {
				return configureSourceDirectoryForSourceSet(path, 'test')
			}
		}

		/**
		 * Configuration interface for repositories.
		 */
		@TupleConstructor(defaults = false)
		class RepositoriesConfig {

			/**
			 * Adds the Maven Central and Maven Central Snapshots repositories to the
			 * project.
			 */
			RepositoriesConfig useMavenCentralAndSnapshots() {
				project.repositories {
					mavenCentral()
					maven {
						name = 'Maven Central Snapshots'
						url = 'https://central.sonatype.com/repository/maven-snapshots/'
					}
				}
				return this
			}
		}

		/**
		 * Configuration interface for testing.
		 */
		@TupleConstructor(defaults = false)
		class TestingConfig {

			/**
			 * Configure all test suites to use JUnit Jupiter.
			 */
			TestingConfig useJUnitJupiter() {
				project.pluginManager.withPlugin('testing-base') {
					project.extensions.configure(TestingExtension) { testing ->
						testing.suites.configureEach { JvmTestSuite test ->
							test.useJUnitJupiter()
						}
					}
				}
				return this
			}
		}
	}
}
