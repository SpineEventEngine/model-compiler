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

package io.spine.tools.mc.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

/**
 * A sub-plugin configuring code generation for a programming language.
 */
public abstract class LanguagePlugin: SubPlugin() {

    /**
     * A name of the programming language handled by this plugin in `camelLowerCase`
     * (e.g. `typeScript`).
     */
    protected abstract val languageName: String

    /**
     * A class of the extension object of this plugin.
     */
    protected abstract val extensionClass: Class<*>

    /**
     * Extends the DSL of `modelCompiler` with the [clause][languageName] for the programming
     * language handled by this plugin.
     *
     * @see extensionClass
     */
    override fun apply(project: Project) {
        super.apply(project)
        val outerExtension = project.outerExtension
        (outerExtension as ExtensionAware).extensions.create(languageName, extensionClass)
    }
}
