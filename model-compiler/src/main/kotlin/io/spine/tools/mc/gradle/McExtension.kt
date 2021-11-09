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

import io.spine.logging.Logging
import io.spine.tools.gradle.defaultMainDescriptors
import io.spine.tools.gradle.defaultTestDescriptors
import io.spine.tools.mc.checks.Severity
import io.spine.tools.mc.gradle.McExtension.Companion.name
import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Nested

/**
 * Extends a Gradle project with the [`modelCompiler`][name] block.
 */
public abstract class McExtension {

    /**
     * Language-independent check settings.
     *
     * @see [checks]
     */
    @Nested
    public abstract fun getChecks(): CommonChecks

    /**
     * The absolute path to the Protobuf descriptor set file in the `main` source set.
     *
     * The file must have the `.desc` extension.
     */
    public abstract val mainDescriptorSetFile: RegularFileProperty

    /**
     * The absolute path to the Protobuf descriptor set file in the `test` source set.
     *
     * The file must have the `.desc` extension.
     */
    public abstract val testDescriptorSetFile: RegularFileProperty

    /**
     * Configures the `checks` property using the passed action.
     */
    public fun checks(action: CommonChecks.() -> Unit) {
        action.invoke(getChecks())
    }

    public companion object : Logging {

        /** The name of the extension. */
        public const val name: String = "modelCompiler"

        /**
         * Adds this extension to the given [Project] and initializes the default values.
         */
        internal fun createIn(p: Project): Unit = with(p) {
            val extensionName: String = this@Companion.name
            _debug().log("Adding the `$extensionName` extension to the project `$p`.")
            val extension = extensions.create(extensionName, McExtension::class.java)
            with (extension) {
                mainDescriptorSetFile.convention(regularFile(defaultMainDescriptors))
                testDescriptorSetFile.convention(regularFile(defaultTestDescriptors))

                getChecks().defaultSeverity.set(Severity.WARN)
            }
        }

        private fun Project.regularFile(file: File) =
            layout.projectDirectory.file(file.toString())
    }
}
