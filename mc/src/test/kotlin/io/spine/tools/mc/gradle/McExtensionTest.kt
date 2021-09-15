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
import io.spine.testing.Correspondences.type
import io.spine.tools.mc.gradle.given.AbstractConfig
import io.spine.tools.mc.gradle.given.TestConfig
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class `McExtension should` {

    private lateinit var ext: McExtension

    @BeforeEach
    fun prepareExtension() {
        val project = ProjectBuilder.builder()
            .withName("ext-test")
            .build()
        project.group = "org.example"
        project.version = "42"
        project.apply(mapOf("plugin" to "java"))
        McPlugin().apply(project)
        ext = project.extensions.getByType(McExtension::class.java)
    }

    @Test
    fun `provide default mainDescriptorSetFile`() {
        val file = ext.mainDescriptorSetFile.asFile.get()
        assertThat(file.path)
            .endsWith("/build/descriptors/main/org.example_ext-test_42.desc")
    }

    @Test
    fun `provide default testDescriptorSetFile`() {
        val file = ext.testDescriptorSetFile.asFile.get()
        assertThat(file.path)
            .endsWith("/build/descriptors/test/org.example_ext-test_42_test.desc")
    }

    @Test
    fun `allow to register custom language-specific configs`() {
        ext.forLanguage<TestConfig> {
            payload = "foo bar"
        }
        val configs = ext.languageConfigurations
        assertThat(configs)
            .comparingElementsUsing(type<LanguageSpecificExtension>())
            .containsExactly(TestConfig::class.java)
        val testConfig = configs.first() as TestConfig
        assertThat(testConfig.payload)
            .startsWith("foo")
    }

    @Test
    fun `allow to register abstract configs`() {
        ext.forLanguage<AbstractConfig> {
            property.set(ext.testDescriptorSetFile.get())
        }
        val configs = ext.languageConfigurations
        assertThat(configs)
            .comparingElementsUsing(type<LanguageSpecificExtension>())
            .containsExactly(AbstractConfig::class.java)
        val config = configs.first() as AbstractConfig
        assertThat(config.property.isPresent)
            .isTrue()
    }

    @Test
    fun `obtain a language-specific config by type`() {
        ext.forLanguage<TestConfig> {
            payload = "aaa"
        }
        val config = ext.languageConfig(TestConfig::class.java)
        assertThat(config)
            .isNotNull()
        assertThat(config!!.payload)
            .startsWith("a")
    }
}