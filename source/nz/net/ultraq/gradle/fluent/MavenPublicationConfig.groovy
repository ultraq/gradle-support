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

import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.tasks.bundling.Jar

/**
 * Configuration interface for putting together Maven publishing artifacts in
 * Gradle.
 *
 * @author Emanuel Rabina
 */
interface MavenPublicationConfig extends MavenCentralEntry {

	/**
	 * Adds a {@code groovydocJar} task, making it part of the bundle to publish.
	 * It will be given a `javadoc` classifier so that it can be used as the
	 * documentation companion for the compiled code, and so that services like
	 * <a href="https://javadoc.io">javadoc.io</a> can find it.  The task will
	 * also have a dependency on the {@code assemble} lifecycle task so it can be
	 * created alongside other artifact outputs.
	 */
	MavenPublicationConfig addGroovydocJar()

	/**
	 * Adds the main software component to the bundle which can be optionally
	 * configured with the given closure.
	 */
	MavenPublicationConfig addJar()

	/**
	 * Adds the main software component to the bundle which can be optionally
	 * configured with the given closure.
	 */
	MavenPublicationConfig addJar(@DelegatesTo(Jar) Closure configure)

	/**
	 * Adds the {@code sourcesJar} task and makes it part of the bundle to
	 * publish.
	 */
	MavenPublicationConfig addSourcesJar()

	/**
	 * Configure the POM that will get published.  The Gradle project {@code name}
	 * and {@code description} properties will also be used for their respective
	 * POM elements.
	 */
	MavenPomConfig configurePom()

	/**
	 * Configure the POM that will get published.  The Gradle project {@code name}
	 * and {@code description} properties will also be used for their respective
	 * POM elements.
	 */
	MavenPomConfig configurePom(@DelegatesTo(MavenPom) Closure configure)
}
