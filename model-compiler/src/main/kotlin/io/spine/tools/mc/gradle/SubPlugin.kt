/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

@file:Suppress("DEPRECATION") // Still need to use until the migration is complete.

package io.spine.tools.mc.gradle

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

/**
 * A Gradle plugin which relies on existence of [McPlugin] (the outer plugin) in the project to
 * which the sub-plugin is going to be applied.
 *
 * More specifically, a sub-plugin needs an instance of [ModelCompilerOptions], which the plugin is going to
 * consume or extend. If the outer plugin is not yet applied, it's automatically created and
 * [applied][apply] to the project by the sub-plugin.
 */
@Deprecated(message = "Please use `io.spine.tools.gradle.lib.LibraryPlugin` instead.")
public abstract class SubPlugin: Plugin<Project> {

    /**
     * Verifies if [McPlugin] is available in the given project. If not, creates and applies it.
     */
    @OverridingMethodsMustInvokeSuper
    override fun apply(project: Project) {
        if (project.outerExtension == null) {
            val outerPlugin = McPlugin()
            outerPlugin.apply(project)
        }
    }

    /**
     * Obtains an instance of the outer extension from the given project.
     */
    protected val Project.outerExtension: ModelCompilerOptions?
        get() = extensions.findByType()
}
