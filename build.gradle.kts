/*
 * Copyright 2020, TeamDev. All rights reserved.
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

@file:Suppress("RemoveRedundantQualifierName") // To prevent IDEA replacing FQN imports.

import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.FindBugs
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Truth
import io.spine.internal.gradle.PublishingRepos
import io.spine.internal.gradle.Scripts
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.spinePublishing
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories.applyStandard()

plugins {
    `java-library`
    idea
    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id).version(version)
    }
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id).version(version)
    }
    kotlin("jvm") version io.spine.internal.dependency.Kotlin.version
}

spinePublishing {
    projectsToPublish.addAll(
        ":mc",
        ":tool-base",
        ":plugin-base",
        ":plugin-testlib"
    )
    targetRepositories.addAll(
        PublishingRepos.cloudRepo,
        PublishingRepos.cloudArtifactRegistry
    )
    // Skip the `spine-` part of the artifact name to avoid collisions with the currently "live"
    // versions.
    spinePrefix.set(false)
}

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
        from("$rootDir/version.gradle.kts")
    }

    group = "io.spine.tools"
    version = extra["versionToPublish"]!!
}

subprojects {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("net.ltgt.errorprone")
        plugin("pmd-settings")
        plugin(Protobuf.GradlePlugin.id)

        from(Scripts.javacArgs(project))
        from(Scripts.projectLicenseReport(project))
        from(Scripts.testOutput(project))
        from(Scripts.javadocOptions(project))

        from(Scripts.testArtifacts(project))
    }

    repositories.applyStandard()

    dependencies {
        errorprone(ErrorProne.core)
        errorproneJavac(ErrorProne.javacPlugin)

        compileOnlyApi(FindBugs.annotations)
        compileOnlyApi(CheckerFramework.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }

        implementation(Guava.lib)

        testImplementation(Guava.testLib)
        JUnit.api.forEach { testImplementation(it) }
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }

    configurations.forceVersions()
    configurations.excludeProtobufLite()

    val javaVersion = JavaVersion.VERSION_1_8

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlin {
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs = listOf(
                "-Xskip-prerelease-check",
                "-Xjvm-default=all",
                "-Xopt-in=kotlin.contracts.ExperimentalContracts"
            )
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    val generatedDir = "$projectDir/generated"

    protobuf {
        generatedFilesBaseDir = generatedDir
        protoc {
            artifact = Protobuf.compiler
        }
    }

    tasks.clean {
        delete(generatedDir)
    }

    apply {
        from(Scripts.slowTests(project))
        from(Scripts.testOutput(project))
        from(Scripts.javadocOptions(project))
    }
}

apply {
    // Generate a repository-wide report of 3rd-party dependencies and their licenses.
    from(Scripts.repoLicenseReport(project))

    // Generate a `pom.xml` file containing first-level dependency of all projects in the repository.
    from(Scripts.generatePom(project))
}

afterEvaluate {
    // Aggregated coverage report across all subprojects.
    apply(from = Scripts.jacoco(project))
}
