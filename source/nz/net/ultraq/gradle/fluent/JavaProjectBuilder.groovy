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

import org.gradle.api.Action
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Configuration interface for putting together a Java project in Gradle.
 *
 * @author Emanuel Rabina
 */
interface JavaProjectBuilder extends JavaProjectSourceBuilderEntry, JavaProjectVerificationBuilderEntry {

	/**
	 * Sets the version of Java to use in the toolchain configuration.
	 */
	JavaProjectBuilder useJavaVersion(int version)

	/**
	 * Adds the Maven Central and Snapshots repositories to the project by
	 * applying the {@link nz.net.ultraq.gradle.UseMavenCentralRepositoriesPlugin} plugin.
	 */
	JavaProjectBuilder useMavenCentralRepositories()

	/**
	 * Configure the {@code jar} task.
	 */
	JavaProjectBuilder withJarOptions(Action<? extends Jar> configure)

	/**
	 * Pass any compilation options to the `compileJava` task.
	 */
	JavaProjectBuilder withJavaCompileOptions(Action<? extends JavaCompile> configure)

	/**
	 * Adds a javadoc JAR archive as output for the build.
	 */
	JavaProjectBuilder withJavadocJar()

	/**
	 * Adds a javadoc JAR archive as output for the build.
	 */
	JavaProjectBuilder withJavadocJar(Action<? extends Jar> configure)

	/**
	 * Adds a sources JAR archive as output for the build.
	 */
	JavaProjectBuilder withSourcesJar()

	/**
	 * Adds a sources JAR archive as output for the build.
	 */
	JavaProjectBuilder withSourcesJar(Action<? extends Jar> configure)
}
