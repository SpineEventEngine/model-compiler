/*
 * Copyright 2022, TeamDev. All rights reserved.
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

import com.google.common.truth.Truth.assertThat
import io.spine.tools.mc.gradle.given.McCobolExtension
import io.spine.tools.mc.gradle.given.McCobolPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`LanguagePlugin` should")
internal class LanguagePluginSpec {

    lateinit var project: Project

    @BeforeEach
    fun createProject() {
        project = ProjectBuilder.builder()
            .withName("lang-plugin-test")
            .build()
    }

    @Test
    fun `add outer plugin, if missing`() {
        assertThat(outerExtension).isNull()

        McCobolPlugin().apply(project)

        assertThat(outerExtension).isNotNull()
    }

    @Test
    fun `take outer plugin, if already applied`() {
        McPlugin().apply(project)
        assertThat(outerExtension).isNotNull()

        McCobolPlugin().apply(project)

        assertThat(nestedExtension).isNotNull()
    }

    companion object {
        /** As defined by [McCobolPlugin]. */
        internal const val languageName: String = "cobol"
    }

    @Nested
    inner class `add language extension` {

        @BeforeEach
        fun applyPlugin() {
            val plugin = McCobolPlugin()
            plugin.apply(project)
        }

        @Test
        fun `using the name`() {
            assertThat(nestedByName).isNotNull()
        }

        @Test
        fun `with the specified class`() {
            assertThat(nestedByName!!.dialect).isNotNull()
        }

        private val nestedByName: McCobolExtension?
            get() = (outerExtension as ExtensionAware).extensions
                .findByName(languageName) as McCobolExtension?
    }

    private val outerExtension: ModelCompilerOptions?
        get() = project.extensions.findByType()

    private val nestedExtension: McCobolExtension?
        get() = (outerExtension as ExtensionAware).extensions.findByType()
}
