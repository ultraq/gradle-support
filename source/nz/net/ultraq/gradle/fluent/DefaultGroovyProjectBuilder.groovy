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

import nz.net.ultraq.gradle.UseMavenCentralRepositoriesPlugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.testing.base.TestingExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Implementation for configuring a Groovy project.
 *
 * @author Emanuel Rabina
 */
class DefaultGroovyProjectBuilder implements GroovyProjectBuilder, GroovyProjectSourceBuilder, GroovyProjectVerificationBuilder {

	protected final Project project

	DefaultGroovyProjectBuilder(Project project) {

		this.project = project
		project.pluginManager.apply('groovy')
		project.tasks.named('groovydoc', Groovydoc) { groovydoc ->
			groovydoc.link('https://docs.groovy-lang.org/latest/html/gapi/', 'groovy.', 'org.apache.groovy.')
		}
		project.pluginManager.withPlugin('idea') {
			var idea = project.extensions.getByName('idea') as IdeaModel
			idea.module {
				outputDir = project.file('build/classes/groovy/main')
				testOutputDir = project.file('build/classes/test')
			}
		}
	}

	@Override
	GroovyProjectSourceBuilder configureSource() {

		return this
	}

	@Override
	GroovyProjectVerificationBuilder configureVerification() {

		return this
	}

	@Override
	GroovyProjectSourceBuilder expand(String filePattern, Map<String, Object> replacements) {

		project.tasks.named('processResources', ProcessResources) { processResources ->
			processResources.filesMatching(filePattern) { file ->
				file.expand(replacements)
			}
		}
		return this
	}

	@Override
	GroovyProjectSourceBuilder expandExtensionModuleVersion(String propertyName = 'moduleVersion', String value = project.version) {

		return expand('**/org.codehaus.groovy.runtime.ExtensionModule', [(propertyName): value])
	}

	@Override
	GroovyProjectVerificationBuilder useCodenarc(TextResource codenarcConfig) {

		project.pluginManager.apply('codenarc')
		project.extensions.configure(CodeNarcExtension) { codenarc ->
			codenarc.config = codenarcConfig
		}
		return this
	}

	@Override
	GroovyProjectVerificationBuilder useJacoco() {

		project.pluginManager.apply('jacoco')
		project.tasks.named('test').configure { test ->
			test.finalizedBy('jacocoTestReport')
		}
		project.tasks.named('jacocoTestReport', JacocoReport) { jacocoTestReport ->
			jacocoTestReport.dependsOn('test')
			jacocoTestReport.reports { reports ->
				reports.xml.required.set(true)
			}
		}
		return this
	}

	@Override
	GroovyProjectBuilder useJavaVersion(int version) {

		project.extensions.configure(JavaPluginExtension) { java ->
			java.toolchain.languageVersion.set(JavaLanguageVersion.of(version))
		}
		project.tasks.named('groovydoc', Groovydoc) { groovydoc ->
			groovydoc.link("https://docs.oracle.com/en/java/javase/${version}/docs/api/java.base/", 'java.', 'javax.')
		}
		return this
	}

	@Override
	GroovyProjectVerificationBuilder useJUnitJupiter() {

		project.extensions.configure(TestingExtension) { testing ->
			testing.suites.configureEach { JvmTestSuite test ->
				test.useJUnitJupiter()
			}
		}
		return this
	}

	@Override
	GroovyProjectBuilder useMavenCentralRepositories() {

		project.pluginManager.apply(UseMavenCentralRepositoriesPlugin)
		return this
	}

	@Override
	GroovyProjectSourceBuilder withDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

		project.dependencies(configure)
		return this
	}

	/**
	 * Sets a single source directory for both source and resource files in
	 * the named sourceset.
	 */
	private void withDirectoryForSourceSetAt(File path, String name) {

		project.extensions.configure(SourceSetContainer) { sourceSets ->
			sourceSets.named(name) { sourceSet ->
				[sourceSet.java, sourceSet.extensions.getByType(GroovySourceDirectorySet), sourceSet.resources]*.srcDirs = [path]
				sourceSet.resources.exclude('**/*.java', '**/*.groovy')
			}
		}
	}

	@Override
	GroovyProjectBuilder withGroovyCompileOptions(Action<? extends GroovyCompile> configure) {

		project.tasks.named('compileGroovy', GroovyCompile, configure)
		return this
	}

	@Override
	GroovyProjectBuilder withGroovydocJar() {

		var groovydocJar = project.tasks.register('groovydocJar', Jar) { groovydocJar ->
			groovydocJar.description = 'Assembles a jar archive containing the main groovydoc.'
			groovydocJar.group = 'build'
			groovydocJar.dependsOn('groovydoc')
			groovydocJar.from(project.tasks.named('groovydoc', Groovydoc).get().destinationDir)
			groovydocJar.destinationDirectory.set(project.layout.buildDirectory.dir('libs'))
			groovydocJar.archiveClassifier.set('javadoc')
		}
		project.tasks.named('assemble') { assembleTask ->
			assembleTask.dependsOn(groovydocJar.get())
		}
		return this
	}

	@Override
	GroovyProjectBuilder withGroovydocOptions(Action<? extends Groovydoc> configure) {

		project.tasks.named('groovydoc', Groovydoc, configure)
		return this
	}

	@Override
	GroovyProjectBuilder withJarOptions(Action<? extends Jar> configure) {

		project.tasks.named('jar', Jar, configure)
		return this
	}

	@Override
	GroovyProjectBuilder withJavaCompileOptions(Action<? extends JavaCompile> configure) {

		project.tasks.named('compileJava', JavaCompile, configure)
		return this
	}

	@Override
	GroovyProjectBuilder withShadowJar(Action<? extends ShadowJar> configure) {

		project.pluginManager.apply('com.gradleup.shadow')
		project.tasks.named('shadowJar', ShadowJar, configure)
		return this
	}

	@Override
	GroovyProjectBuilder withSourcesJar() {

		project.extensions.configure(JavaPluginExtension) { java ->
			java.withSourcesJar()
		}
		return this
	}

	@Override
	GroovyProjectSourceBuilder withSourceDirectory(Object path) {

		withDirectoryForSourceSetAt(project.file(path), 'main')
		project.tasks.withType(Jar).configureEach { jar ->
			jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}
		return this
	}

	@Override
	GroovyProjectVerificationBuilder withTestDependencies(@DelegatesTo(DependencyHandler) Closure configure) {

		project.dependencies(configure)
		return this
	}

	@Override
	GroovyProjectVerificationBuilder withTestDirectory(Object path) {

		withDirectoryForSourceSetAt(project.file(path), 'test')
		return this
	}
}
