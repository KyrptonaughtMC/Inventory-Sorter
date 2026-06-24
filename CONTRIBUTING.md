# Contributing

This project uses [Stonecraft](https://stonecraft.meza.gg) as the main build system, which uses [Stonecutter](https://stonecutter.kikugie.dev/wiki/) under the hood. This means that traditional gradle understanding might not be enough.

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

## Translation management

### In the codebase

In the codebase we use `Component.translatable(key)` function for **CLIENT SIDE ONLY** text.
For text that can come from the **SERVER SIDE** for clients that don't have the mod installed (like command feedback), we use the `ServerComponent.lang(player.getLanguage()).translatable(key)` function.

### In the translation files

We use the `en_us.json` file as the source of truth for all translations. All other translation files are generated from it via Crowdin. DO NOT MODIFY OTHER TRANSLATION FILES DIRECTLY. If you want to add a new translation, add it to the `en_us.json` files and let the Crowdin sync process handle the rest.

## Verifying Changes

### Quick Check

To make sure that the project tests and builds correctly:

- `./gradlew test buildAndCollect`

### Full E2E Check

- `./gradlew chiseledGameTest`

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
