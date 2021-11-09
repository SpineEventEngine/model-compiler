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
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class `'McExtension' should` {

    lateinit var ext: McExtension

    @BeforeEach
    fun prepareExtension() {
        val project = ProjectBuilder.builder()
            .withName("ext-test")
            .build()
        project.group = "org.example"
        project.version = "42"
        project.apply(mapOf("plugin" to "java"))
        McPlugin().apply(project)
        ext = project.extensions.getByType()
    }

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

        private fun currentSeverity() = ext.getChecks().defaultSeverity.get()
    }
}
