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

import com.google.common.truth.Truth.assertThat
import io.spine.tools.mc.checks.Severity
import java.io.File
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class `'ModelCompilerOptions' should` {

    lateinit var project: Project
    lateinit var ext: ModelCompilerOptions

    @BeforeEach
    fun prepareExtension() {
        project = ProjectBuilder.builder()
            .withName("ext-test")
            .build()
        project.group = "org.example"
        project.version = "42"
        project.apply(mapOf("plugin" to "java"))
        McPlugin().apply(project)
        ext = project.extensions.getByType()
    }

    @Test
    fun `register itself with the name`() {
        val found = project.extensions.findByName(ModelCompilerOptions.name)

        assertThat(found).isInstanceOf(ModelCompilerOptions::class.java)
    }

    @Test
    fun `extend 'Project' with 'modelCompiler' property`() {
        assertThat(project.modelCompiler)
            .isInstanceOf(ModelCompilerOptions::class.java)
    }

    @Test
    fun `provide the configuration action for 'modelCompiler'`() {
        project.modelCompiler {
            it.checks { defaultSeverity.set(Severity.OFF) }
        }
        assertThat(project.modelCompiler.checks.defaultSeverity.get())
            .isEqualTo(Severity.OFF)
    }

    /**
     * The path values hard-coded in the test below are composed using
     * the artifact coordinates that match those specified in [prepareExtension].
     */
    @Nested
    inner class `provide default` {

        @Test
        fun mainDescriptorSetFile() {
            val file = ext.mainDescriptorSetFile.asFile.get()
            assertPath(file)
                .endsWith("/build/descriptors/main/org.example_ext-test_42.desc")
        }

        @Test
        fun testDescriptorSetFile() {
            val file = ext.testDescriptorSetFile.asFile.get()
            assertPath(file)
                .endsWith("/build/descriptors/test/org.example_ext-test_42_test.desc")
        }

        private fun assertPath(file: File) = assertThat(file.invariantSeparatorsPath)
    }

    @Nested
    inner class `configure language-neutral 'checks'` {

        @Test
        fun `having 'WARN' as the default severity level`() {
            val severity = currentSeverity()
            assertThat(severity)
                .isEqualTo(Severity.WARN)
        }

        @Test
        fun `with custom severity level`() {
            val expected = Severity.ERROR
            ext.checks {
                defaultSeverity.set(expected)
            }

            val severity = currentSeverity()
            assertThat(severity)
                .isEqualTo(expected)
        }

        private fun currentSeverity() = ext.checks.defaultSeverity.get()
    }
}
