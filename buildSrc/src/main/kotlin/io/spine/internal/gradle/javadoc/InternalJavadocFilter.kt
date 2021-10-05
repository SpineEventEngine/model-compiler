/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.internal.gradle.javadoc

import io.spine.internal.gradle.javadoc.InternalJavadocFilter.Companion.taskName
import io.spine.internal.gradle.sourceSets
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

/**
 * A helper routine which configures the GitHub Pages updater to exclude `@Internal` types.
 */
class InternalJavadocFilter(val version: String) {

    companion object {

        /**
         * The name of the helper task which configures the Javadoc processing
         * to exclude `@Internal` types.
         */
        const val taskName = "noInternalJavadoc"
    }

    /**
     * Creates a custom Javadoc task for the [project] which excludes the the types
     * annotated as `@Internal`.
     *
     * The task is registered under [taskName].
     */
    fun registerTask(project: Project) {
        val excludeInternalDoclet = project.registerConfiguration(version)
        project.appendCustomJavadocTask(excludeInternalDoclet)
    }
}

private fun Project.registerConfiguration(filterVersion: String): Configuration {
    val doclet = ExcludeInternalDoclet(filterVersion)
    return doclet.addTo(this)
}

private fun Project.appendCustomJavadocTask(excludeInternalDoclet: Configuration) {
    val javadocTask = tasks.javadocTask()
    tasks.register(taskName, Javadoc::class.java) {

        source = sourceSets.getByName("main").allJava.filter {
            !it.absolutePath.contains("generated")
        }.asFileTree

        classpath = javadocTask.classpath

        options {
            encoding = JavadocConfig.encoding.name

            // Doclet fully qualified name.
            doclet = "io.spine.tools.javadoc.ExcludeInternalDoclet"

            // Path to the JAR containing the doclet.
            docletpath = excludeInternalDoclet.files.toList()
        }

        val docletOptions = options as StandardJavadocDocletOptions
        JavadocConfig.registerCustomTags(docletOptions)
    }
}
