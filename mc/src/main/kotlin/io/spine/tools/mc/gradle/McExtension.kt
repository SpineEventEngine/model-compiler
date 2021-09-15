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
import io.spine.tools.mc.gradle.McExtension.Companion.name
import java.io.File
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty

/**
 * Extends a Gradle project with the [`modelCompiler`][name] block.
 */
abstract class McExtension {

    private val languageSpecificConfig: MutableMap<String, LanguageSpecificExtension> =
        mutableMapOf()

    /**
     * The Model Compiler configurations specific for certain target languages.
     */
    internal val languageConfigurations: Set<LanguageSpecificExtension>
        get() = languageSpecificConfig.values.toSet()

    /**
     * The absolute path to the main Protobuf descriptor set file.
     *
     * The file must have the `.desc` extension.
     */
    abstract val mainDescriptorSetFile: RegularFileProperty

    /**
     * The absolute path to the test Protobuf descriptor set file.
     *
     * The file must have the `.desc` extension.
     */
    abstract val testDescriptorSetFile: RegularFileProperty

    private lateinit var project: Project

    /**
     * Configure Model Compiler specifically for the given target language, e.g. Java, JavaScript,
     * Dart, etc.
     *
     * This method is a Kotlin-specific API. Use the overload from Java and Groovy.
     */
    inline fun <reified T : LanguageSpecificExtension> forLanguage(noinline config: T.() -> Unit) {
        contract {
            callsInPlace(config, EXACTLY_ONCE)
        }
        val cls = T::class.java
        forLanguage(cls, config)
    }

    /**
     * Configure Model Compiler specifically for the given target language, e.g. Java, JavaScript,
     * Dart, etc.
     *
     * When using this API from Kotlin, consider a Kotlin-specific overload (an inlined method with
     * a reified type parameter).
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : LanguageSpecificExtension> forLanguage(cls: Class<T>, config: (T) -> Unit) {
        contract {
            callsInPlace(config, EXACTLY_ONCE)
        }
        val key = key(cls)
        if (!languageSpecificConfig.containsKey(key)) {
            val newInstance = project.objects.newInstance(cls)
            languageSpecificConfig[key] = newInstance
        }
        val ext = languageSpecificConfig[key]!! as T
        config(ext)
    }

    /**
     * Obtains the Model Compiler configuration specific for a certain target language.
     *
     * Returns `null` if the Model Compiler hasn't been configured for the given language.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : LanguageSpecificExtension> languageConfig(cls: Class<T>): T? {
        return languageSpecificConfig[key(cls)] as T?
    }

    private fun key(cls: Class<*>) = cls.canonicalName

    companion object : Logging {

        const val name = "modelCompiler2"

        /**
         * Adds this extension to the given [Project] and initializes the default values.
         */
        internal fun createIn(p: Project): Unit = with(p) {
            _debug().log("Adding the `$name` extension to the project `$p`.")
            val extension = extensions.create(name, McExtension::class.java)
            extension.project = p
            extension.mainDescriptorSetFile
                .convention(regularFile(defaultMainDescriptors))
            extension.testDescriptorSetFile
                .convention(regularFile(defaultTestDescriptors))
        }

        private fun Project.regularFile(file: File) =
            layout.projectDirectory.file(file.toString())
    }
}

/**
 * A part of the Model Compiler configuration specific for a certain target language.
 *
 * It's recommended to name the implementation classes after the programming languages they
 * represent, for example `Java` or `Dart`.
 *
 * Implementation classes must be open for inheritance and have a public zero-argument constructor
 * annotated with `javax.inject.Inject`. Gradle instantiates them via `project.getObjects()`.
 *
 * When needed, implementation classes may also declare Gradle properties. In this case, both
 * the implementation class and the property must be `abstract`. Gradle will take care of
 * instantiating the properties.
 */
interface LanguageSpecificExtension
