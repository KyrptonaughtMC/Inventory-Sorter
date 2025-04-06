# Configuration

Inventory Sorter's configuration settings are stored in:

```
config/inventorysorter.json
```

The same file is used on both client and server, but the interpretation of its values differs.

## Client Configuration

When the mod is installed on the client, this file stores **user preferences** such as:

- Whether to show the sort button
- Which inventory should be sorted
- Tooltip visibility
- Preferred sorting method

These preferences affect only the player who owns the client. 
They are editable in-game using the configuration menu (see [Usage Guide](/usage-guide#config-menu)) or by editing the JSON file directly.

:::warning
Client preferences cannot override server-defined rules.
:::

## Server Configuration

When the mod is installed on the server, the same file is used to define **compatibility constraints** that apply to all players.

The server only uses these keys:

```
"customCompatibilityListDownloadUrl": "",
"preventSortForScreens": [],
"hideButtonsForScreens": []
```

These define global rules for all players, regardless of whether they have the client mod installed.

Server rules take precedence over player preferences and are enforced unconditionally.

Please refer to the [Admin Guide](/admin-guide) for more details on server configuration.

## Default config file

Below is a complete version of the default configuration. This is the file you will find in `config/inventorysorter.json` after installing the mod.

```json
{
  "showSortButton": true,
  "showTooltips": true,
  "separateButton": true,
  "sortPlayerInventory": false,
  "sortType": "NAME",
  "enableDoubleClickSort": true,
  "sortHighlightedItem": true,
  "customCompatibilityListDownloadUrl": "",
  "preventSortForScreens": [],
  "hideButtonsForScreens": []
}
```

The following sections describe each key in detail.


## Config Keys

### `showSortButton`
**GUI label:** *Display button in inventory*  
**Config file key:** `showSortButton`  
**Default:** `true` (On)

Controls whether a sort button is displayed in compatible inventory screens.
This affects only the button’s visibility, not whether sorting can happen.

When **On (`true`)**, the sort button(s) appear(s) in any inventory that supports it.  
When **Off (`false`)**, the button(s) is hidden entirely, but sorting is still available via other methods (keybind, double-click, or command).

---

### `showTooltips`
**GUI label:** *Display Sort Button Tooltip*  
**Config file key:** `showTooltips`  
**Default:** `true` (On)

Controls whether a tooltip is shown when hovering over the sort button.

When **On (`true`)**, hovering the sort button displays the current sort type.  
When **Off (`false`)**, no tooltip is shown.

---

### `sortHighlightedItem`
**GUI label:** *Sorting sorts mouse hovered inventory*  
**Config file key:** `sortHighlightedItem`  
**Default:** `true` (On)

Controls which inventory is affected by the keybind or double-click.
This does not affect sorting via the sort button, which always targets its assigned inventory.

When **On (`true`)**, sorting only affects the inventory under your mouse cursor.  
When **Off (`false`)**, sorting applies to both the container and the player inventory.

If both this setting and `sortPlayerInventory` are enabled:
- Only the inventory under the mouse is sorted.
- If the mouse is not over an inventory area, the keybind will sort both the player inventory and the open container.

---

### `sortPlayerInventory`
**GUI label:** *Sort player inventory while another inventory is open*  
**Config file key:** `sortPlayerInventory`  
**Default:** `true` (On)

Controls whether your player inventory is included when sorting another container.

When **On (`true`)**, sorting actions apply to both the open container and your player inventory (unless overridden by other settings).  
When **Off (`false`)**, sorting applies only to the container you sorted.

This setting works in combination with `sortHighlightedItem`.

If both this setting and `sortHighlightedItem` are enabled:
- Only the inventory under the mouse is sorted.
- If the mouse is not over an inventory area, the keybind will sort both the player inventory and the open container.

---

### `separateButton`
**GUI label:** *Always display button in player inventory*  
**Config file key:** `separateButton`  
**Default:** `false` (Off)

Determines whether a second sort button is shown next to the player inventory in dual-inventory screens (like chests or crafting tables).
This setting is useful if you want finer control over which inventory to sort using the button.

When **On (`true`)**, two buttons are shown—one for the open container, and one for your own inventory.  
When **Off (`false`)**, only a single button appears, usually for the open container.

This is particularly useful when you have the `sortPlayerInventory` setting enabled, as you will only need the one button to sort both inventories.

---

### `sortType`
**GUI label:** *Sorting method*  
**Config file key:** `sortType`  
**Default:** `"NAME"`

Determines how items are ordered when sorted. This setting affects all sorting methods (button, keybind, double-click, commands).

Available options:
- `"NAME"` – Sorts items alphabetically by name.
- `"CATEGORY"` – Groups by creative tab, then sorts by name.
- `"MOD"` – Groups by the mod that added the item, then sorts by name.
- `"ID"` – Sorts by internal item ID.

:::info
Regardless of the selected method, **Name** is always used as a secondary sort to maintain consistent ordering within groups.
:::

---

### `enableDoubleClickSort`
**GUI label:** *Double click slot to sort inventory*  
**Config file key:** `enableDoubleClickSort`  
**Default:** `true` (On)

Allows sorting an inventory by double-clicking an **empty slot** inside it.<br/>
This method works for all players as long as the server has the mod installed.

When **On (`true`)**, double-clicking an empty slot triggers sorting for that inventory.  
When **Off (`false`)**, double-clicking does nothing.

---


### `preventSortForScreens`
**GUI section:** *Compatibility*  
**Config file key:** `preventSortForScreens`  
**Default:** `[]` (empty list)

Defines a list of screen identifiers where **all sorting is disabled**, including button, keybind, double-click, and commands.

This setting is enforced by the server and applies to all players.  
Client-defined entries only affect the local player.

Each entry must be a screen ID, such as:

```
minecraft:generic_9x3
```

Can be managed with the command: `/invsort nosort add/remove/list` while looking at a container you want to add or remove.

The screens added to the nosort list show up in the GUI in the Compatibility Config screen.

---

### `hideButtonsForScreens`
**GUI section:** *Compatibility*  
**Config file key:** `hideButtonsForScreens`  
**Default:** `[]` (empty list)

Defines a list of screens where the sort button should not be shown.

This setting is respected only by clients that have the mod installed.  
Server-defined entries override client preferences and force hiding for all players.

Sorting by keybind, double-click, or command remains functional, this only affects the button’s visibility.

You can hide the button for a specific screen by `CTRL+clicking` the button while looking at that screen. 
Check the [Usage Guide](/usage-guide#ctrlclick-to-hide) for more details.

The screens with hidden buttons show up in the GUI in the Compatibility Config screen.

---

:::tip
For both the `preventSortForScreens` and `hideButtonsForScreens` settings, you can use the `/invsort screenid` command to
get the screen ID of the container you are looking at. This is useful if you're editing the config file manually and want to
ensure you have the correct screen ID.
:::

---

### `customCompatibilityListDownloadUrl`
**GUI section:** *Compatibility*  
**Config file key:** `customCompatibilityListDownloadUrl`  
**Default:** `""` (empty string)

Specifies a remote URL to fetch a shared compatibility config.

When set, the file is fetched on startup and **merged in memory** with the rest of the config.  
It is not saved to disk.

This allows server operators or groups of players to share consistent compatibility setups across machines.

Manage via:

- `/invsort remote set [url]`
- `/invsort remote clear`
- `/invsort remote show`

:::info
To learn more about how to set up a remote config, check the [Admin Guide](/admin-guide).
:::

---

## Editing the Config File Manually

The configuration file is a standard JSON file.
This means you can edit it with any text editor, but be careful to follow the JSON syntax rules.
- Use double quotes (`"`) for keys and string values.
- Use commas (`,`) to separate key-value pairs.
- Do not use trailing commas after the last item in an object or array. 

Make sure to use valid JSON syntax, or the mod may fail to load. 
:::tip
If you are not familiar with JSON, consider using the in-game configuration menu or commands instead. 
The in-game menu provides a user-friendly interface or the commands provide convenience for modifying settings without 
needing to edit the file directly. 
:::

If you make a mistake while editing the file, the mod may not load correctly. In that case, you can delete the file and let the mod regenerate it with default settings.

You can validate your JSON file using online tools like [JSONLint](https://jsonlint.com/) or [JSON Formatter & Validator](https://jsonformatter.curiousconcept.com/).

