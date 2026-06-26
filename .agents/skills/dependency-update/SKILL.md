---
name: Dependency Update
description: MUST CONSULT When asked to update and refresh dependencies in the project.
---

This project uses Stonecutter to manage multiple Minecraft versions and loaders. Please refer to the [Contributing Guide](../../CONTRIBUTING.md) for more information on how to work with Stonecutter.

The dependency versions are separated per supported Minecraft version.

## Locating the version files

All the version files are in the `versions/dependencies` folder. Each file is named after the Minecraft version it supports, for example `1.19.4.properties` or `26.2.properties`, and contains the dependencies for that version.

## Looking up a dependency

The versions are used in the `build.gradle.kts` file to determine which dependencies to use for each Minecraft version. The dependencies are defined in the `dependencies` block, and the versions are looked up from the corresponding properties file.

Your sources for available versions are the repositories in the build.gradle.kts file.

## Your task

When asked to update a dependency, you should:

1. Take the first versions/dependency/*.properties file that contains the dependency to update that you haven't updated yet.
2. Check the current version of the first dependency you haven't updated yet in the file.
3. Look up the latest version for the given Minecraft version in the repositories defined in the build.gradle.kts file. - you might need to look online or on Modrinth to find out the correct version/loader combination.
4. Update the version in the properties file.
5. Repeat from step 2 until there are no more dependencies to update in the file.
6. Repeat from step 1 until all the properties files have been updated.
7. Once all the properties files have been updated, run `./gradlew buildAndCollect` to make sure everything builds correctly.
8. Stop and notify the user
