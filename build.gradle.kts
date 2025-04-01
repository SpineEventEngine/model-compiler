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

import io.spine.dependency.build.CheckerFramework
import io.spine.dependency.build.ErrorProne
import io.spine.dependency.build.FindBugs
import io.spine.dependency.lib.Guava
import io.spine.dependency.test.JUnit
import io.spine.dependency.local.Spine
import io.spine.dependency.local.Base
import io.spine.dependency.local.Logging
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.test.Truth
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.VersionWriter
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.github.pages.updateGitHubPages
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.kotlin.setFreeCompilerArgs
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.report.coverage.JacocoConfig
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator
import io.spine.gradle.standardToSpineSdk
import io.spine.gradle.testing.configureLogging
import io.spine.gradle.testing.registerTestTasks
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `java-library`
    kotlin("jvm")
    jacoco
    idea
    `project-report`
    protobuf
    errorprone
    `gradle-doctor`
}

spinePublishing {
    modules = setOf("model-compiler")
    destinations = with(PublishingRepos) {
        setOf(
            gitHub("model-compiler"),
            cloudArtifactRegistry
        )
    }
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

    repositories.standardToSpineSdk()
}

subprojects {
    applyPlugins()
    setDependencies()
    forceConfigurations()
    setupKotlin()
    setupProtobuf()
    configureTesting()
    setupDocPublishing()
}

JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)

typealias Subproject = Project

fun Subproject.applyPlugins() {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("net.ltgt.errorprone")
        plugin("org.jetbrains.dokka")
        plugin("pmd-settings")
        plugin(Protobuf.GradlePlugin.id)
    }
    apply<IncrementGuard>()
    apply<VersionWriter>()

    CheckStyleConfig.applyTo(project)
    JavadocConfig.applyTo(project)
    LicenseReporter.generateReportIn(project)
}

@Suppress("UnstableApiUsage")
fun Subproject.forceConfigurations() {
    configurations {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                force(
                    io.spine.dependency.test.JUnit.runner,
                    Base.lib,
                    Logging.lib,
                    Logging.middleware,
                )
            }
        }
    }
}

fun Project.setDependencies() {
    dependencies {
        errorprone(ErrorProne.core)

        compileOnlyApi(FindBugs.annotations)
        compileOnlyApi(CheckerFramework.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }

        implementation(Guava.lib)

        testImplementation(Guava.testLib)
        JUnit.api.forEach { testImplementation(it) }
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }
}

fun Subproject.setupKotlin() {
    kotlin {
        explicitApi()
        compilerOptions {
            jvmTarget.set(BuildSettings.jvmTarget)
            setFreeCompilerArgs()
        }
    }
}

fun Subproject.setupProtobuf() {
    protobuf {
        generatedFilesBaseDir = "$projectDir/generated"
        protoc {
            artifact = Protobuf.compiler
        }
        tasks.clean {
            delete(generatedFilesBaseDir)
        }
    }
}

fun Subproject.setupDocPublishing() {
    tasks {
        val dokkaJavadoc by getting(DokkaTask::class)
        register("javadocJar", Jar::class) {
            from(dokkaJavadoc.outputDirectory)
            archiveClassifier.set("javadoc")
            dependsOn(dokkaJavadoc)
        }
    }

    updateGitHubPages(Spine.base) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}

fun Subproject.configureTesting() {
    tasks {
        registerTestTasks()
        test.configure {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            configureLogging()

            // See https://github.com/gradle/gradle/issues/18647.
            jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.util=ALL-UNNAMED"
            )
        }
    }
}
