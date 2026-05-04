# Contributing

This project uses [Stonecraft](https://stonecraft.meza.gg) as the main build system, which uses [Stonecutter](https://stonecutter.kikugie.dev/wiki/) under the hood. This means that traditional gradle understanding might not be enough.

## COMMENTS ARE SPECIAL

We're using [Stonecutter](https://stonecutter.kikugie.dev/wiki/) to manage multiple Minecraft versions and loaders.

Stonecutter enhances the coding process by being a preprocessor for the code. The preprocessor is managed via comments.

DO NOT ASSUME THAT COMMENTED OUT CODE IS DEAD CODE.

It's more likely to be a different Minecraft version/loader path managed by Stonecutter.

## Working on specific Minecraft version/loader

We're using [Stonecutter](https://stonecutter.kikugie.dev/wiki/) to manage multiple Minecraft versions and loaders.

### Switching versions

Gradle has a "Set active project to <version>-<loader>" tasks, those are the ones to use.

The versions are defined in the `settings.gradle.kts` file.

### Running tasks against the active version

- `./gradlew buildActive` - build just the current active version
- `./gradlew testActiveServer` - run the current active version's server tests

## Verifying Changes

### Quick Check

To make sure that the project tests and builds correctly:

- `./gradlew test buildAndCollect`

### Full E2E Check

- `./gradlew chiseledGameTest`

### DO NOT

Do not run traditional gradle compile tasks. The project uses a custom build process that includes additional steps beyond compilation. Running standard compile tasks may lead to incomplete builds and test failures.

## Documentation

- For the project, look in the docs folder.
- For fabric, use: https://docs.fabricmc.net/develop/
- For neoforge, use: https://docs.neoforged.net/docs/gettingstarted/
- For Minecraft: use the embedded code itself
- For Stonecraft: https://stonecraft.meza.gg/
- For Stonecutter: https://stonecutter.kikugie.dev/wiki/
