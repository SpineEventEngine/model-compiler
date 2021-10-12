# Spine Model Compiler

This repository provides the common base for language-specific build-time tools for Spine.

## Structure

`tool-base` provides common components for building build-time tools, including file manipulations,
Protobuf reflection, simple code generation, etc.

`plugin-base` provides abstractions for building Gradle plugins.

`plugin-testlib` provides test fixtures for Gradle plugins.

`mc` is the base Model Compiler Gradle plugin, which constitutes the shared language-agnostic parts
of the "Greater" Model Compiler

## Related repositories

See [SpineEventEngine/mc-java](https://github.com/SpineEventEngine/mc-java) for
the Java-specific tools.
