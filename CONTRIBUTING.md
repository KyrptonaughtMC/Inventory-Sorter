# Contributing

This project uses [Stonecraft](https://stonecraft.meza.gg) as the main build system, which uses [Stonecutter](https://stonecutter.kikugie.dev/wiki/) under the hood. This means that traditional gradle understanding might not be enough.

## Full support matrix

For every task, including investigation, design, implementation, review, and verification, consider every supported Minecraft version–loader combination. Derive the build targets from [settings.gradle.kts](settings.gradle.kts) and inspect the corresponding files in [versions/dependencies](versions/dependencies) for actual Minecraft versions, loader dependencies, and additional advertised versions. The active project selects a working target; it does not define the support scope.

Assess each change against every declared target, including paths inactive in the current checkout. Preserve shared behavior and use the project's Stonecutter conditionals, constants, and replacements where Minecraft versions or loaders differ. Inspect existing preprocessing rules in [build.gradle.kts](build.gradle.kts) and the affected source branches before changing API names, imports, dependencies, or apparently inactive code. Apply this requirement to tests, resources, and configuration as well as production code.

Additional advertised Minecraft versions are compatibility claims, not separate build targets unless settings declares them. Consider those claims when assessing compatibility; a build against one patch version does not establish runtime behavior on every advertised patch version.

## COMMENTS ARE SPECIAL

We're using [Stonecutter](https://stonecutter.kikugie.dev/wiki/) to manage multiple Minecraft versions and loaders.

Stonecutter enhances the coding process by being a preprocessor for the code. The preprocessor is managed via comments.

DO NOT ASSUME THAT COMMENTED OUT CODE IS DEAD CODE.

It's more likely to be a different Minecraft version/loader path managed by Stonecutter.

## Adding support for a new Minecraft version

The good people at Neoforge publish excellent porting guides.
Find the version corresponding to the Minecraft version you want to port to, and follow the instructions there. [Minecraft Porting Guide](https://github.com/ChampionAsh5357/neoforged-github/tree/port/26.2/primers)

Alternatively also check the Fabric change posts: https://fabricmc.net/blog/

## Working on specific Minecraft version/loader

We're using [Stonecutter](https://stonecutter.kikugie.dev/wiki/) to manage multiple Minecraft versions and loaders.

### Switching versions

Gradle has a "Set active project to <version>-<loader>" tasks, those are the ones to use.

The versions are defined in the `settings.gradle.kts` file.

### Running tasks against the active version

- `./gradlew buildActive` - build just the current active version
- `./gradlew testActiveServer` - run the current active version's server tests

These tasks provide focused feedback for the active target. Success on that target does not establish compatibility across the full support matrix. Use the supported Gradle tasks to switch the active project; do not manually edit the generated active-project declaration in `stonecutter.gradle.kts`.

## Translation management

### In the codebase

In the codebase we use `Component.translatable(key)` function for **CLIENT SIDE ONLY** text.
For text that can come from the **SERVER SIDE** for clients that don't have the mod installed (like command feedback), we use the `ServerComponent.lang(player.getLanguage()).translatable(key)` function.

### In the translation files

We use the `en_us.json` file as the source of truth for all translations. All other translation files are generated from it via Crowdin. DO NOT MODIFY OTHER TRANSLATION FILES DIRECTLY. If you want to add a new translation, add it to the `en_us.json` files and let the Crowdin sync process handle the rest.

## Verifying Changes

Choose verification appropriate to the changed surface: behavior tests for production behavior, content and link checks for documentation, and parser or tool-native checks for configuration. Full-matrix consideration applies to every change; it does not require running production tests for documentation-only edits.

For build and behavior checks, confirm that the selected tasks cover every declared version–loader target and the relevant checks. Inspect the configured task scope rather than inferring coverage from a task name or prefix. Report which targets and checks were verified and any that remain unverified. Do not declare full-matrix verification complete from active-project results alone.

### Quick Check

To make sure that the project tests and builds correctly:

- `./gradlew test buildAndCollect`

### Full E2E Check

- `./gradlew chiseledGameTest`

### CI development check

The development-build step in [.github/workflows/build.yml](.github/workflows/build.yml) invokes:

- `./gradlew chiseledGameTest chiseledBuildAndCollect --stacktrace`

Use this as the repository's CI command reference for GameTests and artifact builds. The quick check, active-target checks, and CI command cover different kinds of verification; confirm target coverage before treating any invocation as full-matrix evidence.

### Test Coverage

New code is expected to aim for 100% unit test coverage. Code in `src/main/java/net/kyrptonaught/inventorysorter/sort` remains the clearest example of this expectation because it is shared sorting core, so regressions there affect both server-side sorting and client fallback sorting.

Coverage is a guardrail, not a license to make the design worse. When Minecraft types, registries, components, tags, or other runtime state make unit testing awkward, inspect the Minecraft code first and look for existing test examples in this project before adding indirection. Sometimes Minecraft already provides a small API that can be used directly in unit tests.

If direct unit testing would force sorting code to depend on hard-to-create Minecraft state, a small adapter may be extracted so the logic remains testable. Only do this when the adapter preserves or improves leanness. Do not add broad abstractions, fake platforms, or test-only architecture just to satisfy a coverage number.

If a line cannot be covered cleanly without harming the design, document the reason in the change discussion and cover the behavior at the closest practical level, such as a focused unit test around extracted logic plus a GameTest for the Minecraft integration. This should be rare, explicit, and justified by the code shape rather than convenience.

### DO NOT

Do not run traditional gradle compile tasks. The project uses a custom build process that includes additional steps beyond compilation. Running standard compile tasks may lead to incomplete builds and test failures.

## Documentation

- For the project, look in the docs folder.
- For fabric, use: https://docs.fabricmc.net/develop/
- For neoforge, use: https://docs.neoforged.net/docs/gettingstarted/
- For Minecraft: use the embedded code itself
- For Stonecraft: https://stonecraft.meza.gg/
- For Stonecutter: https://stonecutter.kikugie.dev/wiki/
- Minecraft version porting guides: https://github.com/ChampionAsh5357/neoforged-github/tree/port/26.2/primers
