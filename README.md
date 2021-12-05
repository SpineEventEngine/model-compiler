# Spine Model Compiler

[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![codecov.io](https://codecov.io/github/SpineEventEngine/model-compiler/coverage.svg?branch=master)](https://codecov.io/github/SpineEventEngine/model-compiler?branch=master) &nbsp;
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

[gh-actions]: https://github.com/SpineEventEngine/model-compiler/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/model-compiler/actions/workflows/build-on-ubuntu.yml/badge.svg

This repository provides the common base for language-specific build-time tools for Spine.

## Structure

`model-compiler` is the base Model Compiler Gradle plugin, which constitutes the shared
language-agnostic parts of the "Greater" Model Compiler.

## JVM Version

The source code in this repository is built with Java 11.

## Related repositories

* [SpineEventEngine/tool-base](https://github.com/SpineEventEngine/tool-base) —
common code for build-time tools.

* [SpineEventEngine/mc-java](https://github.com/SpineEventEngine/mc-java) —
Java-specific tools.

