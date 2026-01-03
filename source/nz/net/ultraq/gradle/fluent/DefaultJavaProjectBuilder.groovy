/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.gradle.UseMavenCentralRepositoriesPlugin

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

import groovy.transform.CompileStatic
import javax.inject.Inject

/**
 * Implementation for configuring a Java project.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class DefaultJavaProjectBuilder implements JavaProjectBuilder, JavaProjectSourceBuilder, JavaProjectVerificationBuilder {

	protected final Project project

	@Inject
	DefaultJavaProjectBuilder(Project project) {

		this.project = project
		project.pluginManager.apply('java')
		project.pluginManager.withPlugin('idea') {
			var idea = project.extensions.getByName('idea') as IdeaModel
			idea.module {
				outputDir = project.file('build/idea/classes/java/main')
				testOutputDir = project.file('build/idea/classes/test')
			}
		}
	}

	@Override
	JavaProjectSourceBuilder configureSource() {

		return this
	}

	@Override
	JavaProjectVerificationBuilder configureVerification() {

		return this
	}

	@Override
	JavaProjectSourceBuilder expand(String filePattern, Map<String, ?> replacements) {

		project.tasks.named('processResources', ProcessResources) { processResources ->
			processResources.filesMatching(filePattern) { file ->
				file.expand(replacements)
			}
		}
		return this
	}

	@Override
	JavaProjectVerificationBuilder useJacoco() {

		project.pluginManager.apply('jacoco')
		project.tasks.named('test').configure { test ->
			test.finalizedBy('jacocoTestReport')
		}
		project.tasks.named('jacocoTestReport', JacocoReport) { jacocoTestReport ->
			jacocoTestReport.dependsOn('test')
			jacocoTestReport.reports.xml.required.set(true)
		}
		return this
	}

	@Override
	JavaProjectBuilder useJavaVersion(int version) {

		project.extensions.configure(JavaPluginExtension) { java ->
			java.toolchain.languageVersion.set(JavaLanguageVersion.of(version))
		}
		return this
	}

	@Override
	JavaProjectVerificationBuilder useJUnitJupiter() {

		project.extensions.configure(TestingExtension) { testing ->
			testing.suites.withType(JvmTestSuite).configureEach { suite ->
				suite.useJUnitJupiter()
			}
		}
		return this
	}

	@Override
	JavaProjectBuilder useMavenCentralRepositories() {

		project.pluginManager.apply(UseMavenCentralRepositoriesPlugin)
		return this
	}

	@Override
	JavaProjectSourceBuilder withDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

		project.dependencies(configure)
		return this
	}

	/**
	 * Sets a single source directory for both source and resource files in
	 * the named sourceset.
	 */
	protected void withDirectoryForSourceSetAt(File path, String name) {

		project.extensions.configure(SourceSetContainer) { sourceSets ->
			sourceSets.named(name) { sourceSet ->
				[sourceSet.java, sourceSet.resources].each { sourceDirectorySet ->
					sourceDirectorySet.srcDirs = [path]
				}
				sourceSet.resources.exclude('**/*.java')
			}
		}
	}

	@Override
	JavaProjectBuilder withJarOptions(Action<? extends Jar> configure) {

		project.tasks.named('jar', Jar, configure)
		return this
	}

	@Override
	JavaProjectBuilder withJavaCompileOptions(Action<? extends JavaCompile> configure) {

		project.tasks.named('compileJava', JavaCompile, configure)
		return this
	}

	@Override
	JavaProjectBuilder withJavadocJar(Action<? extends Jar> configure = null) {

		project.extensions.configure(JavaPluginExtension) { java ->
			java.withJavadocJar()
			if (configure) {
				project.tasks.named('javadocJar', Jar, configure)
			}
		}
		return this
	}

	@Override
	JavaProjectBuilder withSourcesJar(Action<? extends Jar> configure = null) {

		project.extensions.configure(JavaPluginExtension) { java ->
			java.withSourcesJar()
			if (configure) {
				project.tasks.named('sourcesJar', Jar, configure)
			}
		}
		return this
	}

	@Override
	JavaProjectSourceBuilder withSourceDirectory(Object path) {

		withDirectoryForSourceSetAt(project.file(path), 'main')
		project.tasks.withType(Jar).configureEach { jar ->
			jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}
		return this
	}

	@Override
	JavaProjectVerificationBuilder withTestDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

		project.dependencies(configure)
		return this
	}

	@Override
	JavaProjectVerificationBuilder withTestDirectory(Object path) {

		withDirectoryForSourceSetAt(project.file(path), 'test')
		return this
	}
}
