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
They are editable in-game using the [configuration menu](/usage-guide#config-menu) or by editing the JSON file directly.

Player sorting preferences are synchronized to a supporting server for commands and double-click sorting.
With a vanilla client, use the player commands instead; those preferences are saved per player on that server.

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

This excerpt shows default values for the main sorting controls and target-selection settings.
The generated file also contains other options.

```json
{
  "showSortButton": true,
  "showTooltips": true,
  "separateButton": true,
  "sortPlayerInventory": false,
  "allowPlayerInventorySorting": true,
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
**Default:** `true` (on)

Controls whether a sort button is displayed in compatible inventory screens.
This affects only the button’s visibility, not whether sorting can happen.

When **on (`true`)**, the sort button(s) appear(s) in any inventory that supports it.  
When **off (`false`)**, the button(s) is hidden entirely, but sorting is still available via other methods (keybind, double-click, or command).

---

### `showTooltips`
**GUI label:** *Display Sort Button Tooltip*  
**Config file key:** `showTooltips`  
**Default:** `true` (on)

Controls whether a tooltip is shown when hovering over the sort button.

When **on (`true`)**, hovering the sort button displays the current sort type.  
When **off (`false`)**, no tooltip is shown.

---

### `sortHighlightedItem`

GUI label: Only Sort the Inventory Under the Mouse<br/>
Config file key: `sortHighlightedItem`<br/>
Default: `true` (on)

Chooses the primary target of the sort keybind when a sortable container is open:

- when on, hovering a player-inventory slot targets the player inventory; a container slot or empty space targets the container
- when off, the keybind targets the container regardless of the hovered slot

A container-targeted sort can also include the player inventory when `sortPlayerInventory` is on.
With only the player-inventory screen open, the keybind targets the player inventory regardless of this setting.

This setting does not change a sort button's target or the server's double-click target, which follows the clicked slot.
The [sorting behavior reference](/usage-guide#sorting-behavior-reference) shows these combinations.

With a vanilla client, change the saved server preference with:
```
/invsort sortHighlightedInventory on|off
```

And you can check the current value with:
```
/invsort sortHighlightedInventory
```

---

### `sortPlayerInventory`

GUI label: Also Sort Player Inventory When Sorting Containers<br/>
Config file key: `sortPlayerInventory`<br/>
Default: `false` (off)

Controls automatic inclusion of your player inventory when a container is sorted by a button, keybind, or double-click.
The `/invsort sort` command sorts only the targeted container.

When on, a container-targeted action also sorts your player inventory, provided `allowPlayerInventorySorting` is on.
When off, a container-targeted action sorts only the container.

Turning this off does not disable direct player-inventory sorts from a player button, keybind, double-click, or `/invsort sortme`.
Use [`allowPlayerInventorySorting`](#allowplayerinventorysorting) to disable both direct and automatic player sorts.

With a vanilla client, change the saved server preference with:
```
/invsort sortPlayerInventory on|off
```

And you can check the current value with:
```
/invsort sortPlayerInventory
```

---

### `allowPlayerInventorySorting`

GUI label: Allow Player Inventory Sorting<br/>
Config file key: `allowPlayerInventorySorting`<br/>
Type: boolean (`true` or `false`)<br/>
Default: `true` (on), including when the field is omitted from an existing config or saved player data

When off, Inventory Sorter does not sort your player inventory through buttons, the sort keybind, double-clicks, or `/invsort sortme`.
It also blocks automatic player sorts alongside container sorts, even if `sortPlayerInventory` is on.
Player sort buttons are hidden. Container sorting and its existing restrictions are unchanged.

A keybind that targets the disabled player inventory does nothing; it does not switch to sorting the container.
Pending client-fallback player sorts are discarded before they start. Already-sent clicks are not undone.

To turn player sorting off using the client mod:

1. Open the [config menu](/usage-guide#config-menu).
2. In the Logic category, turn Allow Player Inventory Sorting off and save.

The preference is saved in the client config and synchronized to supporting servers.
It applies to client fallback when the server does not have Inventory Sorter.
Full enforcement of server commands and server double-click sorting requires a server version that supports this setting.
Older servers cannot enforce this preference.

To check support, look for Allow Player Inventory Sorting in the client config menu and run `/invsort allowPlayerInventorySorting` on the server.
A supporting server reports whether player sorting is on or off.
If the command is unknown or you cannot access it, do not assume server enforcement; ask the server operator.

With Inventory Sorter installed on a supporting server, modded and vanilla clients can use:

```text
/invsort allowPlayerInventorySorting off
/invsort allowPlayerInventorySorting on
/invsort allowPlayerInventorySorting
```

The first two commands disable or enable player sorting; the last shows the current preference.
The command updates only the issuing player's saved server settings and synchronizes the value to a supporting client mod.
With a vanilla client, it remains a per-player, per-server preference.
With a supporting client mod, the synchronized value is also saved to that client's config.

This is a player preference, not a global server rule: setting this field in a dedicated server's config does not disable player sorting for everyone.
It is separate from container deny lists and from hiding a sort button with `separateButton` or `showSortButton`.

---

### `separateButton`

GUI label: Sort Button in Player Inventory<br/>
Config file key: `separateButton`<br/>
Default: `true` (on)

Determines whether a second sort button is shown next to the player inventory in dual-inventory screens (like chests or crafting tables).
This setting is useful if you want finer control over which inventory to sort using the button.

When **on (`true`)**, two buttons are shown—one for the open container, and one for your own inventory.  
When **off (`false`)**, only a single button appears, usually for the open container.

This is particularly useful when you have the `sortPlayerInventory` setting enabled, as you will only need the one button to sort both inventories.

This controls button visibility, not permission to sort. Player buttons remain hidden when `allowPlayerInventorySorting` is off.

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
Regardless of the selected sort type, item **Name** is always used as a secondary sort.
This ensures consistent ordering within grouped or categorized sections.
:::


---

### `enableDoubleClickSort`
**GUI label:** *Double click slot to sort inventory*  
**Config file key:** `enableDoubleClickSort`  
**Default:** `true` (on)

Allows sorting an inventory by double-clicking an **empty slot** inside it.<br/>
This method works for all players as long as the server has the mod installed.

When **on (`true`)**, double-clicking an empty slot triggers sorting for that inventory.  
When **off (`false`)**, double-clicking does nothing.

Can be changed with the command: 
```
/invsort doubleClickSort on|off
```

And you can check the current value with: 
```
/invsort doubleClickSort
```

---

### `requireModifierToScroll`  
**GUI label:** *Require holding Left Control to change sorting method*  
**Config file key:** `requireModifierToScroll`  
**Default:** `false` (off)  

When **on (`true`)**, you must hold `Left Control` while scrolling to change the sorting method.  
When **off (`false`)**, scrolling changes the sorting method without needing to hold any keys.

---

### `preventSortForScreens`
**GUI section:** *Compatibility*  
**Config file key:** `preventSortForScreens`  
**Default:** `[]` (empty list)

Defines container menu identifiers whose container inventory cannot be sorted through buttons, keybinds, double-clicks, or commands.
This does not disable sorting of the player inventory while that container is open.
Use [`allowPlayerInventorySorting`](#allowplayerinventorysorting) for a complete player-inventory opt-out on supporting clients and servers.

This setting is enforced by the server and applies to all players.  
Client-defined entries only affect the local player.

Each entry must be a screen ID, such as:

```
minecraft:generic_9x3
```

Can be managed with the command while looking at a container you want to add or remove.

To add or remove a container:
```
/invsort nosort add/remove
```

To check the current list of screens:
```
/invsort nosort list
```


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
Check the [button-hiding guidance](/usage-guide#hiding-the-main-sort-button) for more details.

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

For client preferences, including `allowPlayerInventorySorting`:

1. Close Minecraft before editing `config/inventorysorter.json`.
2. Save the file with your changes.
3. Start Minecraft and join your world or server again.

The client loads the edited preferences on startup and synchronizes them when joining a supporting server.
Changing the file on disk while Minecraft is running does not apply the preference immediately.
`/invsort reload` alone does not synchronize edited client preferences to the server.
For an immediate change during play, use the config menu and save, or use the player preference command.

For dedicated-server compatibility rules, follow [Reloading the Server Config](/admin-guide#reloading-the-server-config).
Those global rules are separate from player sorting preferences.

JSON syntax rules:

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
