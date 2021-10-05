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

package io.spine.internal.gradle.github.pages

import io.spine.internal.gradle.Cli
import io.spine.internal.gradle.RepoSlug
import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection

/**
 * Performs the update of GitHub pages.
 */
fun Task.updateGhPages(project: Project) {
    val plugin = project.plugins.getPlugin(UpdateGitHubPages::class.java)

    // Create SSH config file to allow pushing commits to the repository.
    val rootFolder = plugin.rootFolder
    val gitHubAccessKey = gitHubKey(rootFolder)
    registerSshKey(rootFolder, gitHubAccessKey)

    val ghRepoFolder = File("${plugin.checkoutTempFolder}/${Branch.ghPages}")
    val gitHost = RepoSlug.fromVar().gitHost()
    checkoutDocs(rootFolder, gitHost, ghRepoFolder)

    val docDirPostfix = "reference/$project.name"
    val mostRecentDocDir = File("$ghRepoFolder/$docDirPostfix")
    logger.debug("Replacing the most recent docs in `$mostRecentDocDir`.")
    val generatedDocs = project.files(plugin.javadocOutputPath)
    copyDocs(project, generatedDocs, mostRecentDocDir)

    val versionedDocDir = File("$mostRecentDocDir/v/$project.version")
    logger.debug("Storing the new version of docs in the directory `$versionedDocDir`.")
    copyDocs(project, generatedDocs, versionedDocDir)

    Cli(ghRepoFolder).execute("git", "add", docDirPostfix)
    configureCommitter(ghRepoFolder)
    commitAndPush(ghRepoFolder, project)
    logger.debug("The GitHub Pages contents were successfully updated.")
}

/**
 * Locates `deploy_key_rsa` in the passed [rootFolder] and returns it as a [File].
 *
 * If it is not found, a [GradleException] is thrown.
 *
 * <p>A CI instance comes with an RSA key. However, of course, the default key has no
 * privileges in Spine repositories. Thus, we add our own RSA key — `deploy_rsa_key`.
 * It must have `write` rights in the associated repository.
 * Also, we don't want that key to be used for anything else but GitHub Pages publishing.
 *
 * Thus, we configure the SSH agent to use the `deploy_rsa_key`
 * only for specific references, namely in `github.com-publish`.
 */
private fun gitHubKey(rootFolder: File): File {
    val gitHubAccessKey = File("${rootFolder.absolutePath}/deploy_key_rsa")

    if (!gitHubAccessKey.exists()) {
        throw GradleException(
            "File $gitHubAccessKey does not exist. It should be encrypted" +
                    " in the repository and decrypted on CI."
        )
    }
    return gitHubAccessKey
}

/**
 * Creates an SSH key with the credentials from [gitHubAccessKey]
 * and registers it by invoking the `register-ssh-key.sh` script.
 */
private fun registerSshKey(rootFolder: File, gitHubAccessKey: File) {
    val sshConfigFile = File("${System.getProperty("user.home")}/.ssh/config")
    if (!sshConfigFile.exists()) {
        val parentDir = sshConfigFile.canonicalFile.parentFile
        parentDir.mkdirs()
        sshConfigFile.createNewFile()
    }
    sshConfigFile.appendText(
        System.lineSeparator() +
                "Host github.com-publish" + System.lineSeparator() +
                "User git" + System.lineSeparator() +
                "IdentityFile ${gitHubAccessKey.absolutePath}" + System.lineSeparator()
    )

    Cli(rootFolder).execute(
        "${rootFolder.absolutePath}/config/scripts/register-ssh-key.sh",
        gitHubAccessKey.absolutePath
    )
}

private fun checkoutDocs(rootFolder: File, gitHost: String, repoBaseDir: File) {
    Cli(rootFolder).execute("git", "clone", gitHost, repoBaseDir.absolutePath)
    Cli(repoBaseDir).execute("git", "checkout", Branch.ghPages)
}

private fun copyDocs(project: Project, source: FileCollection, destination: File) {
    destination.mkdir()
    project.copy {
        from(source)
        into(destination)
    }
}

/**
 * Configures Git to publish the changes under "UpdateGitHubPages Plugin" Git user name
 * and email stored in "FORMAL_GIT_HUB_PAGES_AUTHOR" env variable.
 */
private fun configureCommitter(repoBaseDir: File) {
    val cli = Cli(repoBaseDir)
    cli.execute("git", "config", "user.name", "\"UpdateGitHubPages Plugin\"")
    val authorEmail = AuthorEmail.fromVar().toString()
    cli.execute("git", "config", "user.email", authorEmail)
}

private fun commitAndPush(repoBaseDir: File, project: Project) {
    val cli = Cli(repoBaseDir)
    cli.execute(
        "git",
        "commit",
        "--allow-empty",
        "--message=\"Update Javadoc for module ${project.name} as for version ${project.version}\""
    )
    cli.execute("git", "push")
}
