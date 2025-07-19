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

import org.gradle.api.Action
import org.gradle.api.publish.maven.MavenPom

/**
 * Configuration interface for putting together Maven publishing artifacts in
 * Gradle.
 *
 * @author Emanuel Rabina
 */
interface MavenPublicationBuilder extends MavenCentralBuilderEntry {

	/**
	 * Configure the POM that will get published.  The Gradle project {@code name}
	 * and {@code description} properties will also be used for their respective
	 * POM elements.
	 */
	MavenPomBuilder configurePom()

	/**
	 * Configure the POM that will get published.  The Gradle project {@code name}
	 * and {@code description} properties will also be used for their respective
	 * POM elements.
	 */
	MavenPomBuilder configurePom(Action<? extends MavenPom> configure)

	/**
	 * Add the given artifacts to the publication.
	 *
	 * <p>If {@code groovydocJar} is one of the artifacts, it will be given a
	 * `javadoc` classifier so that it can be used as the documentation companion
	 * for the main JAR and so that services like
	 * <a href="https://javadoc.io">javadoc.io</a> can find it.
	 */
	MavenPublicationBuilder withArtifacts(Object... artifacts)
}
