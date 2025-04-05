# Configuration

Inventory Sorter's configuration is stored in multiple places depending on how the mod is installed and used.

## Configuration Sources

### Client Configuration

When the client mod is installed, configuration is loaded from:

```
config/inventorysorter.json
```


This file controls all mod behavior for the player, including sorting logic, input methods, 
and visual settings. If the client mod is present, this file is always the source of truth.
The server cannot override or influence these values when the client mod is present.

Settings can be edited in-game using the configuration menu, accessible through [Mod Menu][mod-menu] if installed.


### Server-Side Player Configuration

If a player connects using a vanilla client (without the mod installed), the server falls back to a configuration attached to their player entity. This allows server operators to define basic behavior such as enabled sorting methods or default sort type for unmodded players.

This fallback configuration is only used when the connecting player does not have the mod installed

### Server-Side Configuration

When running on a dedicated server, only the following fields in `config/inventorysorter.json` are used:

- `preventSortForScreens`
- `hideButtonsForScreens`
- `customCompatibilityListDownloadUrl`

All other values are ignored.
This allows server owners to enforce compatibility or gameplay balancing overrides.

### Compatibility List

Compatibility entries can be loaded from an external source using the command:

```
/invsort downloadCompatibilityList [URL]
```

This stores the URL in the client config. On every game startup, the compatibility list is fetched again and merged into the current configuration. These lists can define screens where sorting should be disabled or the sort button hidden.

If the list cannot be fetched, no fallback is used.

---

### `showSortButton`
**GUI label:** *Display button in inventory*  
**Config file key:** `showSortButton`  
**Default:** `true` (On)

Controls whether sort buttons are visible in any inventory screen.

When **On (`true`)**, a sort button is displayed in compatible inventory screens.  
When **Off (`false`)**, no sort buttons are shown. Sorting is still available through mouse interactions, keybinds, or commands.

This setting only affects button visibility. It does not disable sorting itself.

---

### `showTooltips`
**GUI label:** *Show tooltip when hovering sort button*  
**Config file key:** `showTooltips`  
**Default:** `true` (On)

Controls whether a tooltip appears when you hover over a sort button.

When **On (`true`)**, hovering the sort button will display a tooltip showing the current sort type.  
When **Off (`false`)**, the tooltip will not appear.

This setting only affects the sort button's tooltip. It does not affect sorting behavior.

---

### `separateButton`
**GUI label:** *Always display button in player inventory*  
**Config file key:** `separateButton`  
**Default:** `true` (On)

Controls whether the sort button is shown separately for both the container and the player inventory.

When **On (`true`)**, two buttons are displayed: one for the container (e.g. chest) and one for your inventory.  
When **Off (`false`)**, only a single button is shown, typically for the container you opened.

This helps reduce visual clutter in dual-inventory screens like chests or crafting tables.

---

### `sortPlayerInventory`
**GUI label:** *Sort player inventory while another inventory is open*  
**Config file key:** `sortPlayerInventory`  
**Default:** `false` (Off)

Controls whether sorting actions (such as the sort button or keybind) apply to your player inventory even when another container is open.

When **On (`true`)**, using the sort function will sort your player inventory, even if you have a chest, furnace, or other screen open.  
When **Off (`false`)**, sorting actions will target only the currently opened container.

This setting is especially useful when combined with the sort keybind or button.

---

### `sortType`
**GUI label:** *Sorting method*  
**Config file key:** `sortType`  
**Default:** `"NAME"`

Controls how items are grouped and ordered when sorting an inventory. This setting applies to all sorting methods.

Available options:

- `"NAME"` – Sorts items alphabetically by their display name.
- `"CATEGORY"` – Sorts items by their creative tab category (as defined by Minecraft), then alphabetically within each category.
- `"MOD"` – Sorts items by the mod that added them, then by name.
- `"ID"` – Sorts items by their internal registry ID, which is mostly useful for debugging or technical ordering.

Regardless of the selected method, item names are always used as a secondary sort for consistency within each group.

The current sort type is shown in the tooltip when hovering over the sort button.

---

### `sortHighlightedItem`
**GUI label:** *Sorting sorts mouse hovered inventory*  
**Config file key:** `sortHighlightedItem`  
**Default:** `true` (On)

Determines which inventory is sorted when using the keybind or double-click.

When **On (`true`)**, sorting only affects the inventory currently under the mouse cursor. 
This allows precise control over which inventory is sorted.

When **Off (`false`)**, sorting applies to **both** the opened container (if one exists) and your player inventory.

If both this setting and `sortPlayerInventory` are enabled:
- The mod will only sort the inventory under your cursor
- Your player inventory will only be sorted if your mouse is over it

This setting does not affect sorting triggered by the sort button.

---

### `enableDoubleClickSort`
**GUI label:** *Enable double-click sorting*  
**Config file key:** `enableDoubleClickSort`  
**Default:** `true` (On)

Allows you to sort an inventory by double-clicking an **empty slot** inside it.

When **On (`true`)**, double-clicking an empty slot will trigger a sort for the hovered inventory.  
When **Off (`false`)**, double-clicks are ignored by Inventory Sorter.

This method works with both modded and vanilla clients, as long as the server has the mod installed. Requires the cursor to be over an empty slot within the inventory area.

---


### `preventSortForScreens`
**Config file key:** `preventSortForScreens`  
**Default:** `[]` (empty list)

Specifies a list of screen class names where sorting should be completely disabled. This applies to all sorting methods: sort button, keybind, double-click, and commands.

Each entry must be a fully qualified screen class name, such as:

```
net.minecraft.client.gui.screen.ingame.EnchantmentScreen
```

If the current screen matches an entry in the list, Inventory Sorter will not perform any sorting.

This setting is useful for preventing unexpected behavior in containers that don’t support sorting properly.

---

### `hideButtonsForScreens`
**Config file key:** `hideButtonsForScreens`  
**Default:** `[]` (empty list)

Specifies a list of screen class names where the sort button should be hidden.

This does **not** disable sorting itself. Other methods such as keybinds, double-click, and commands will still work unless also blocked by `preventSortForScreens`.

Each entry must be a fully qualified screen class name, such as:

```
net.minecraft.client.gui.screen.ingame.BeaconScreen
```

Use this to remove the sort button from specific containers without disabling sorting entirely.

---

### `customCompatibilityListDownloadUrl`
**Config file key:** `customCompatibilityListDownloadUrl`  
**Default:** `""` (empty string)

Specifies a URL from which to download an external compatibility list. This list may include values for `preventSortForScreens` and `hideButtonsForScreens`.

When this setting is set:

- The compatibility list is downloaded automatically on game startup
- Its contents are merged with the local config before any sorting occurs
- The file must follow the expected JSON schema

This allows server owners or modpack developers to distribute compatibility rules without requiring users 
to manually configure them. The value is set automatically when a player runs:

```
/invsort downloadCompatibilityList [URL]
```

If the download fails, the currently set configuration is used.

---


[mod-menu]: https://modrinth.com/mod/modmenu
