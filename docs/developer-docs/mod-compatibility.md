# Mod compatibility

Inventory Sorter handles mod compatibility through two mechanisms:

| Need | Mechanism | Starting point |
| --- | --- | --- |
| Let sorting use inventory slots supplied by another mod | Explicit Java integration | `CompatibilityPlugin`, with `TrinketsPlugin` as the example |
| Hide sorting controls or prevent sorting in a particular container | Identifier deny lists | `src/main/resources/data/inventorysorter/hide-buttons.json` and `do-not-sort.json` |

Use an integration when sorting needs to understand another mod's inventory API or slot interaction. Use a deny-list entry when the required behavior is to hide controls or exclude a container from sorting. The mechanisms can coexist: supporting one feature of a mod does not make all of its containers sortable.

## Explicit mod integrations

An integration translates between another mod's inventory API and Inventory Sorter's sorting interfaces. Decide which inventories and interactions are appropriate for that mod, and keep those decisions in its integration. Trinkets inventories should not be sorted; that is a Trinkets-specific decision, not a restriction on integrations for other mods. The [Trinkets plugin](../../src/main/java/net/kyrptonaught/inventorysorter/compat/plugins/TrinketsPlugin.java) is an example of how to structure and load an optional integration.

### Optional loading and dependencies

[CompatibilityPlugins](../../src/main/java/net/kyrptonaught/inventorysorter/compat/CompatibilityPlugins.java) owns the registration list. Each registration contains a mod ID and a plugin class name. On first use, it checks `PlatformServices.PLATFORM.isModLoaded` before reflectively constructing the plugin, then caches the loaded plugin list. A plugin construction or linkage failure is logged and that plugin is omitted.

Keep references to the other mod's API inside the plugin implementation. Register its implementation by class name so optional API classes need not be loaded when the mod is absent. Trinkets also checks mod presence inside its hooks.

The [build configuration](../../build.gradle.kts) declares `trinkets-updated` with `compileOnly`, `localRuntime`, `testCompileOnly`, and `testRuntimeOnly`. This supplies the API for compilation and the mod for development and tests, without declaring it as a bundled production dependency. Its `trinkets_version` is selected from [versions/dependencies](../../versions/dependencies).

### Integration boundary

[CompatibilityPlugin](../../src/main/java/net/kyrptonaught/inventorysorter/compat/CompatibilityPlugin.java) is the current integration contract. Its hooks expose additional server bundle targets, recognize client bundle slots, and prepare those slots before client clicks. Defaults contribute no targets and take no action. These hooks reflect the currently implemented feature; they do not define every possible form of mod compatibility.

Choose the behavior first, then check whether the existing contract can express it. A new integration that needs to sort a mod-owned inventory may need a different hook. Design that contract explicitly rather than forcing unrelated behavior into a bundle hook or treating Trinkets' inventory policy as universal.

Keep mod-specific discovery, slot access, write notification, and interaction preparation in the plugin. Shared sorting logic should consume Inventory Sorter's own interfaces. Server inventory access and client menu-click execution have different responsibilities; account for both when the feature supports both. Client-only API access must remain isolated from dedicated-server paths.

### Adding an integration

1. Establish the mod-specific policy: which inventories may be sorted, which slots may participate, and which interactions must be preserved. Inspect the mod's API and existing integration patterns before selecting the contract.
2. Implement the appropriate integration in `compat/plugins`, keeping optional mod API references and inventory decisions there. Use existing hooks where they fit; explicitly design any required contract extension.
3. Register the implementation by mod ID and class name in `CompatibilityPlugins`. Add the needed compile, development, and test dependencies in the build configuration, with compatible versions in the version properties.
4. Handle differences in mod IDs, APIs, and client/server behavior across the full support matrix with Stonecutter swaps, replacements, or conditionals where appropriate.
5. Verify the intended feature with the mod present and absent, including inventory mutation and synchronization, slot restrictions, and applicable server and client behavior. Follow [CONTRIBUTING.md](../../CONTRIBUTING.md#verifying-changes) for verification and coverage reporting.

## Container deny lists

The shipped lists are plain JSON arrays of menu identifiers:

| File | Effect |
| --- | --- |
| [hide-buttons.json](../../src/main/resources/data/inventorysorter/hide-buttons.json) | Hides sorting buttons for the matching container menu |
| [do-not-sort.json](../../src/main/resources/data/inventorysorter/do-not-sort.json) | Prevents sorting the matching container inventory |

Button visibility and sort permission are independent. Hiding a button does not prohibit sorting through other entry points. Preventing container sorting does not itself hide buttons or disable sorting of the player's inventory while that container is open. Add an identifier to both lists when both effects are required. `guild:quest_screen` is an existing example present in both.

### Choosing the identifier

[InventoryScreenId](../../src/main/java/net/kyrptonaught/inventorysorter/InventoryScreenId.java) derives ordinary container IDs from the registered `MenuType` in `BuiltInRegistries.MENU`. Entries match exact identifiers, such as `guild:quest_screen`; they are not Java screen class names, block IDs, or wildcard patterns.

For a targetable block container, a player with access to `/invsort screenID` can look at the block within six blocks and run the command. Inventory Sorter opens and closes the target's menu while resolving its ID and returns copyable feedback. This command does not inspect arbitrary currently open or portable menus. For those, inspect the other mod's menu registration and confirm the ID used by `InventoryScreenId`.

Player inventory menus use the special identifier constructed from `player_inventory`. They have separate UI and sorting paths; the ordinary container rules described here should not be treated as a global player-inventory sorting switch.

### Loading and enforcement

[LocalLoader](../../src/main/java/net/kyrptonaught/inventorysorter/compat/sources/LocalLoader.java) reads the bundled lists through `getResourceAsStream`. Despite their location under `data`, these files are classpath resources, not a Minecraft datapack reload interface.

[Compatibility](../../src/main/java/net/kyrptonaught/inventorysorter/compat/Compatibility.java) combines deny entries from the bundled lists, predefined vanilla exclusions, the official online lists, local configuration, and an optional remote configuration. Entries are unioned into separate button and sorting sets; removing an entry from one source does not override an exclusion supplied by another source. Per-player sort prevention is checked in addition to the combined sorting set.

Loading runs asynchronously. `reload()` clears the sets and starts loading them again, so completion is not immediate. Errors are logged by the compatibility loader; missing bundled resources contribute empty sets. The official online loader reads these same two files from the repository's `main` branch and contributes empty sets on download failure. Do not rely on network access as the only source of a required exclusion.

[SortButtonDisplayPolicy](../../src/main/java/net/kyrptonaught/inventorysorter/client/SortButtonDisplayPolicy.java) consults button visibility for ordinary container menus. [SortabilityPolicy](../../src/main/java/net/kyrptonaught/inventorysorter/inventory/SortabilityPolicy.java) consults sort prevention for container sorting on the server and in client fallback. An identifier being absent from the deny list is not sufficient to make a container sortable: menu validity, player state, inventory shape, and client slot restrictions also apply.

### Adding an exclusion

1. Confirm the registered menu identifier and which behavior must be excluded.
2. Add it to the appropriate JSON array, retaining valid JSON and avoiding duplicate entries. The existing `guild:quest_screen` entry in both arrays illustrates hiding controls and preventing container sorting.
3. Check whether the identifier differs across supported Minecraft versions or loaders. Inspect the full matrix and any relevant resource preprocessing before assuming one shared entry covers every target.
4. Validate the JSON and verify the intended button and sorting behavior separately, including alternative sort entry points and the player's inventory. Follow [CONTRIBUTING.md](../../CONTRIBUTING.md#verifying-changes) for verification and reporting.

## Stonecraft and Stonecutter support

Apply the [full support matrix requirements](../../CONTRIBUTING.md#full-support-matrix) to both integrations and exclusions. Derive targets from settings and dependency properties rather than from the active checkout.

Trinkets illustrates two preprocessing needs: its `TRINKETS_MOD_ID` has a Stonecutter swap from `trinkets` to `trinkets_updated` on NeoForge, and `build.gradle.kts` replaces the internal `TrinketSlot` class path for versions before 26.3. Inspect swaps, replacements, conditionals, and inactive branches together when adapting an integration; commented branches remain part of supported behavior.

An active-target build is focused feedback. Report actual target and behavior coverage, including anything unverified, before claiming compatibility across the support matrix.
