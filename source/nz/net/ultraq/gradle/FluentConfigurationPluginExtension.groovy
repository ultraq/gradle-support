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

import nz.net.ultraq.gradle.fluent.GroovyProjectConfig
import nz.net.ultraq.gradle.fluent.SourceConfig
import nz.net.ultraq.gradle.fluent.TestingConfig

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.testing.base.TestingExtension

import groovy.transform.TupleConstructor

/**
 * The {@code configure} script block and the entry-point methods for the fluent
 * API.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class FluentConfigurationPluginExtension {

	final Project project

	/**
	 * Starts a fluent chain for configuring a Groovy project.  This will apply
	 * the {@code groovy} plugin.
	 */
	GroovyProjectConfig createGroovyProject() {

		return new DefaultGroovyProjectConfig()
	}

	private class DefaultGroovyProjectConfig implements GroovyProjectConfig, SourceConfig, TestingConfig {

		DefaultGroovyProjectConfig() {

			project.pluginManager.apply('groovy')
		}

		@Override
		GroovyProjectConfig expandExtensionModuleVersion(String propertyName = 'moduleVersion', String value = project.version) {
		SourceConfig expandExtensionModuleVersion(String propertyName = 'moduleVersion', String value = project.version) {

			project.tasks.named('processResources', ProcessResources) { processResources ->
				processResources.filesMatching('**/org.codehaus.groovy.runtime.ExtensionModule') { file ->
					file.expand([(propertyName): value])
				}
			}
			return this
		}

		@Override
		SourceConfig configureSource() {

			return this
		}

		@Override
		TestingConfig configureTesting() {

			return this
		}

		@Override
		GroovyProjectConfig useMavenCentralAndSnapshots() {

			project.repositories.mavenCentral()
			project.repositories.maven {
				name = 'Maven Central Snapshots'
				url = 'https://central.sonatype.com/repository/maven-snapshots/'
			}
			return this
		}

		@Override
		GroovyProjectConfig useJavaVersion(int version) {

			project.extensions.configure(JavaPluginExtension) { java ->
				java.toolchain.languageVersion.set(JavaLanguageVersion.of(version))
			}
			return this
		}

		@Override
		TestingConfig useJUnitJupiter() {

			project.extensions.configure(TestingExtension) { testing ->
				testing.suites.configureEach { JvmTestSuite test ->
					test.useJUnitJupiter()
				}
			}
			return this
		}

		@Override
		SourceConfig withDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

			project.dependencies(configure)
			return this
		}

		/**
		 * Sets a single source directory for both source and resource files in
		 * the named sourceset.
		 */
		private void withDirectoryForSourceSetAt(Object path, String name) {

			project.extensions.configure(SourceSetContainer) { sourceSets ->
				sourceSets.named(name) { sourceSet ->
					[sourceSet.java, sourceSet.extensions.getByType(GroovySourceDirectorySet), sourceSet.resources]*.srcDirs =
						[project.file(path)]
					sourceSet.resources.exclude('**/*.java', '**/*.groovy')
				}
			}
		}

		@Override
		SourceConfig withSourceDirectory(Object path) {

			withDirectoryForSourceSetAt(path, 'main')
			project.afterEvaluate {
				if (project.tasks.names.contains('sourcesJar')) {
					project.tasks.named('sourcesJar', Jar) { jar ->
						jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
					}
				}
			}
			return this
		}

		@Override
		TestingConfig withTestDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

			project.dependencies(configure)
			return this
		}

		@Override
		TestingConfig withTestDirectory(Object path) {

			withDirectoryForSourceSetAt(path, 'test')
			return this
		}
	}
}
