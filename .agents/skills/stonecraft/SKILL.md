---
name: stonecraft
description: MUST USE when working on Minecraft mods
metadata:
  version: "1"
---

# Working with Stonecraft and Stonecutter

## Confirm this project uses Stonecraft

Before following any other instruction in this skill, confirm that Gradle applies the `gg.meza.stonecraft` plugin.

Check `settings.gradle[.kts]`, `stonecutter.gradle[.kts]`, and the shared `build.gradle[.kts]`. Resolve any plugin alias used by those files to confirm the exact plugin ID.

If you cannot confirm that the project applies `gg.meza.stonecraft`, stop using this skill. Do not run Stonecraft tasks or follow the remaining guidance.

## Repository instructions

Read the repository's `AGENTS.md` and contributing guide in full before working. Apply their project-specific requirements in addition to this skill.

## Keep this skill current

Before relying on this installed skill, run:

```bash
./gradlew stonecraftGuidanceVersion
```

Compare the installed and bundled guidance versions reported by the task.

- If the installed version is current, continue using this skill. Do not run `stonecraftGuidance`.
- If the installed version is older, missing, or invalid, run `./gradlew stonecraftGuidance` to print the current bundled skill.
- Explain that an update is available and obtain explicit user approval before modifying the installed skill.
- After approval, merge every non-conflicting bundled change into the installed skill while preserving all user-authored changes.
- If a bundled instruction conflicts with user-authored content, preserve the user-authored content and ask the user to resolve the conflict. Do not silently discard either instruction.
- Use `./gradlew installStonecraftSkill` only when the skill is missing.
- Use `./gradlew installStonecraftSkill --force-overwrite` only when the user explicitly agrees to discard the installed skill's current contents.

## Stonecutter version boundary

The Stonecutter-specific guidance in this skill targets the Stonecutter 0.9.x release line.
If the project uses a release line after 0.9.x, consult the official documentation and release or migration notes for that version before proceeding.

## Matrix model

Every task must consider the full Minecraft version-loader matrix configured through `settings.gradle[.kts]`, including code paths inactive in the current checkout.

Stonecutter preprocesses Minecraft-version and loader-specific branches in the shared source tree. Stonecraft supplies the loader constants and configures the Gradle workflow; Architectury provides the cross-loader tooling.

## COMMENTS ARE SPECIAL - PAY ATTENTION

Stonecutter uses comments to control source branches for different Minecraft versions and loaders.

DO NOT ASSUME THAT COMMENTED OUT CODE IS DEAD CODE.

Commented code may be active in another version-loader node. Before changing or removing it, determine which nodes select it. Do not hand-edit Stonecutter guard comments merely to make the current node compile.

Switching the active project merges the current node's processed state into shared `src/`, selects the requested node, and rewrites the Stonecutter-controlled branches for that node.

## VERSIONED TASKS DO NOT SWITCH SHARED `src/`

Shared `src/` represents the active version-loader node. Other nodes use source states generated from it.

A version-subproject invocation such as:

```bash
./gradlew :versions:<version>-<loader>:<task>
```

runs the task for that node without switching the active project. This is useful when only the task result matters, but shared `src/` may still represent another node. Do not diagnose or edit version-dependent source by comparing that result with the currently active source.

If a version-subproject task reports a failure, switch that node active and reproduce the failure through the active-target workflow before diagnosing it.

## Target workflow

Read the supported targets from `settings.gradle[.kts]`. Before editing, diagnosing, or testing version-dependent source, run Stonecutter's generated task for the required node:

```bash
./gradlew "Set active project to <version>-<loader>"
```

Then verify only that selected node:

```bash
./gradlew buildActive
```

Correct target-specific failures and repeat `buildActive` until the target passes.

## Restore the canonical state

After target-specific work and before full-matrix verification, run:

```bash
./gradlew "Reset active project"
```

This uses Stonecutter's switching workflow to restore the active project to the `vcsVersion` declared in `settings.gradle[.kts]`.

Do not edit the active-project marker in `stonecutter.gradle[.kts]` manually.

Immediately before handover, reset again so the repository remains at `vcsVersion`.

## Full-matrix verification

To run a task across the whole matrix, invoke its unqualified aggregate task from the repository root.

- `./gradlew build` builds and tests the full declared Minecraft version-loader matrix.
- `./gradlew buildAndCollect` does the same and collects the produced jars in one place.
- Do not substitute direct Gradle `compile*` tasks for Stonecraft's build or verification tasks. Compilation alone can bypass required orchestration and produce incomplete or misleading verification.

If a full-matrix build exposes a target-specific failure:

1. Run `./gradlew "Set active project to <version>-<loader>"` for the failing target.
2. Correct and verify that target with `./gradlew buildActive`.
3. Run `./gradlew "Reset active project"`.
4. Rerun the full-matrix build.

## Update dependencies

Dependency versions are separated by canonical Minecraft version. Their manifests live in `versions/dependencies`, with names such as `1.21.4.properties` or `26.2.properties`.

Stonecutter evaluates the shared `build.gradle[.kts]` for every node. Stonecraft loads the dependency manifest matching that node's canonical Minecraft version.

Determine the requested scope before editing:

- For a named dependency update, update only that dependency in every manifest where it is present.
- For an explicitly requested full dependency refresh, update every dependency in every manifest.
- When adding or upgrading a supported Minecraft version, use the Minecraft-version workflow below instead of treating it as an ordinary dependency refresh.

For every dependency and manifest in scope:

1. Check the current version.
2. Resolve the latest version compatible with that manifest's Minecraft version and loader nodes. Check the repositories used by the build and authoritative upstream sources; use online sources such as Modrinth when needed.
3. Update the manifest.
4. Continue until every dependency and manifest in scope has been handled.
5. Reset the active project and verify the full matrix with `./gradlew build`.

## Adding a new Minecraft version

Follow these steps in order.

### 1. Research compatibility

- Check FabricMC release notes for relevant Minecraft changes.
- Use Fabric's developer resources for compatible Fabric Loader, mappings, and Fabric API versions.
- Use NeoForge's project resources for a compatible NeoForge version when the project supports NeoForge.
- Check the build's declared repositories and authoritative upstream sources for every remaining dependency.
- Identify source incompatibilities that require Stonecutter-controlled branches or replacements; do not assume dependency changes alone are sufficient.

### 2. Declare the version-loader nodes

Add the new Minecraft version and every loader supported for it to the `stonecutter.shared` block in `settings.gradle[.kts]`, following the repository's existing `version(...)` or helper pattern.

If adding a newer Minecraft version, update `vcsVersion` to that version's canonical loader node, following the repository's existing canonical-loader convention. When adding an older version, leave `vcsVersion` unchanged.

### 3. Add the dependency manifest

Create `versions/dependencies/<minecraftVersion>.properties`, using the previous version's manifest as the structural reference.

Resolve compatible published artifacts for every key the project uses. Do not silently omit loader-specific or optional dependencies that appear in the reference manifest.

### 4. Build every new target

For each new loader node, use the target workflow:

```bash
./gradlew "Set active project to <minecraftVersion>-<loader>"
./gradlew buildActive
```

Correct incompatibilities and repeat `buildActive` until that target passes.

### 5. Restore and verify

Run:

```bash
./gradlew "Reset active project"
./gradlew build
```

Use `./gradlew buildAndCollect` instead of `build` when the jars also need to be collected.

If aggregate verification exposes another target-specific problem, use the full-matrix failure loop above and rerun aggregate verification.

Immediately before handover, reset once more and confirm that the active project is the declared `vcsVersion`.

## Sources and precedence

- Use the repository's embedded source and existing version branches to understand Minecraft behavior across supported targets.
- Use https://stonecraft.meza.gg/ for Stonecraft behavior.
- Use https://stonecutter.kikugie.dev/wiki/ for Stonecutter behavior.
- Use https://docs.fabricmc.net/develop/ for Fabric development.
- Use https://docs.neoforged.net/docs/gettingstarted/ for NeoForge development.

Match guidance to the Stonecraft and Stonecutter versions applied by the repository. Do not replace repository-specific support policy, loader coverage, verification, or handover rules with assumptions from another project.
