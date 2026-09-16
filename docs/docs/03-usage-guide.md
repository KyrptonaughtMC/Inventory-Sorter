# Usage Guide

Inventory Sorter gives you multiple ways to quickly organize your inventory.
Some features are available out of the box, while others depend on whether the mod is installed on the
client and how it's configured.

Inventory Sorter can sort all vanilla and modded inventories.
_(As long as the modded inventories don't do anything too out of the ordinary.)_

## Available Sort Types

You can choose how inventories are sorted using one of the following modes:

- **Name** (default): Sorts items alphabetically by their display name.
- **Category**: Groups similar items together (e.g., tools, food, blocks).
- **Mod**: Groups items by the mod they come from, then sorts alphabetically.
- **Id**: Sorts items by their internal item ID.

:::info
Regardless of the selected sort type, item **Name** is always used as a secondary sort.
This ensures consistent ordering within grouped or categorized sections.
:::


If you are using the client mod, you can change the sort type in one of three ways:

- [Scroll on the sort button](#scroll-to-change-sort-type) while hovering over it
- Open the configuration screen and select your preferred sort type
- Edit the [JSON config][configuration] file directly

If you are using a vanilla client (or want to automate it), use the command:

`/invsort sortType [category|mod|name|id]`

The sort type is stored per player, not per container.
Once set, it will apply to all supported inventories you sort.

:::note
Your selected sort type is saved between sessions.

**With the client mod**, it's stored on the client and applies across all servers. <br/>
**With a vanilla client**, it's saved *per player per server*, based on your last `/invsort sortType` command.

:::

## Sort Button

If you are using the client mod and the sort button is enabled, a small icon will appear in supported inventories.
Clicking it will instantly sort the contents of that container.

The button looks like this when unfocused: ![Sort Button (Unfocused)](https://raw.githubusercontent.com/KyrptonaughtMC/Inventory-Sorter/main/src/main/resources/assets/inventorysorter/textures/gui/button_unfocused.png)

The sort button must be enabled in the configuration to appear.

![](/img/usage-guide/sort-button.png)

### Hiding the Player Inventory Sort Button

If you have enabled sorting for both inventories at once, you may want to hide the sort button shown next to the player inventory.
Use `separateButton` in the configuration screen or config file to hide that button without disabling player sorting.
To disable player sorting through every Inventory Sorter trigger, use [Allow Player Inventory Sorting](#disabling-player-inventory-sorting).

![](/img/usage-guide/player-inventory-button-config.png)

### Hiding the main Sort Button

Clicking the sort button while holding `Left Control` will hide the button for the current screen.
This is useful if the button overlaps with other UI elements or mods.

The chat message identifies the inventory and includes a clickable **Undo** link. Close the inventory,
open chat, and click **Undo** to re-enable the button. Reopen the inventory to see the button again.
Each Undo link re-enables the button for the inventory named in its message.
If that button is already enabled, the link makes no change.

You can also re-enable the button through the GUI configuration menu or manually update the config file.

:::tip
If you’re trying to restore a hidden sort button but aren’t sure which screen it was hidden from, the last inventory 
you opened will be highlighted in the config menu. This only applies if the inventory was opened within the last 5 minutes, 
helping you quickly locate and re-enable the button without digging through every entry.
:::

### Scroll to Change Sort Type

While hovering over the sort button, scrolling the mouse wheel will cycle through the available sorting methods:
**Category**, **Mod**, **Name**, and **Id**.

![](/img/usage-guide/scroll-type.webp)

You can scroll through the sort types without holding `Left Control`.
You can change this in the configuration via the [`requireModifierToScroll`](/configuration/#requiremodifiertoscroll) setting.

![](/img/usage-guide/modifier-settings.png)

## Keybinds

The client mod allows you to trigger sorting using a configurable keybind.
By default, this key is `P`. You can assign a different key from the Controls menu in Minecraft.

:::tip
If you prefer, you can also assign the keybind to a mouse button, like the **Middle Mouse Button**.
:::

The keybind is only available when the client mod is installed.

### Keybind Options

If you have the client mod installed, there are several keybinds you can configure in Minecraft's controls menu.

:::tip
Minecraft may show a conflict warning if both actions are bound to the same key.
That’s fine. Inventory Sorter handles this safely, you don’t need to separate them.
:::


![](/img/usage-guide/keybinds.png)


#### Sort

Sorts your inventory when inside a container.

#### Open settings

Opens the Inventory Sorter settings when no container is open.  
By default, the Sort key (`P`) also opens the config screen when pressed outside any inventory screen.

## Config menu

With the client mod installed, press the Open Settings key while no inventory screen is open.
The default key is `P`; you can change it in Minecraft's Controls menu.
Change the settings and save to apply them and update `config/inventorysorter.json`.

## Disabling player inventory sorting

In the [config menu](#config-menu), turn Allow Player Inventory Sorting off and save.
This blocks direct player sorts and automatic player sorts alongside container sorts, while leaving container sorting available.
It is different from hiding the player sort button or turning off automatic inclusion with `sortPlayerInventory`.

On a supporting Inventory Sorter server, you can also use:

```text
/invsort allowPlayerInventorySorting off
```

Use `on` to restore player sorting, or omit the argument to check the current preference.
The command works with vanilla clients and saves the preference for that player on that server.
With a supporting client mod, it also updates the client's saved preference.

See the [configuration reference](/configuration#allowplayerinventorysorting) for defaults, synchronization, pending-sort behavior, and older-server limitations.

## Double-Click

You can sort an inventory by double-clicking on an **empty slot**.
This works even with vanilla clients, as long as the server has [Inventory Sorter installed][server-installation]
and the double-click sorting feature is enabled in the [Configuration][configuration].

### Enabling or Disabling Double-Click Sorting

If you're using a vanilla client, or want to change the setting without using the 
[Config Menu](#config-menu) or the [config file][configuration], you can toggle this feature using:

```
/invsort doubleClickSort on
/invsort doubleClickSort off
```

To check the current setting, use:

```
/invsort doubleClickSort
```

This command controls whether double-clicking an empty slot will trigger sorting.


## Sort Commands

Two commands are available to sort inventories directly:

- `/invsort sort` sorts the container you are currently looking at
- `/invsort sortme` sorts your player inventory, unless player sorting is disabled

Commands work with both modded and vanilla clients.
They are always available as long as the server has [Inventory Sorter installed][server-installation].
`/invsort sort` does not automatically include the player inventory.

---

## Bundle Sorting

When sorting into bundles is enabled, Inventory Sorter looks for existing non-empty bundles and moves matching loose items into them before it lays out the rest of the inventory.
Only items that already match the bundle's contents are inserted, and Minecraft's normal bundle rules still apply.

For player inventory sorts, Inventory Sorter can also use:

- Bundles in the player hotbar, when hotbar bundle targets are enabled.
- Bundles in compatible mod slots.

---

## Feature Availability

| Feature           | Requires Client Mod | Works with Vanilla Client | Configurable |
|-------------------|---------------------|---------------------------|--------------|
| Sort Button       | ✅                   | ❌                         | ✅            |
| Keybind           | ✅                   | ❌                         | ✅            |
| Config Menu       | ✅                   | ❌                         | ✅            |
| Double-Click      | ❌                   | ✅                         | ✅            |
| `/invsort sort`   | ❌                   | ✅                         | ❌            |
| `/invsort sortme` | ❌                   | ✅                         | ✅            |

If a feature doesn't seem to work, it may be disabled in the configuration.
See the [Configuration][configuration] section for more information.

---

## Sorting Behavior Reference

Inventory Sorter offers multiple ways to sort your inventory. The behavior depends on which method you use and how the following settings are configured:

- `allowPlayerInventorySorting`: Whether player-inventory sorting is permitted at all.
- `sortPlayerInventory`: Whether container-triggered button, keybind, or double-click sorts also include the player inventory.
- `sortHighlightedItem`: Whether the keybind uses the hovered inventory as its primary target.
- `enableDoubleClickSort`: Whether double-clicking an empty slot triggers sorting.

The tables below assume `allowPlayerInventorySorting = true` and a sortable container is open.
When it is false, remove the player inventory from every result: Container + Player becomes Container only, and Player only becomes no sort.
Existing container restrictions still apply.

### Sort Button

Sort buttons are displayed next to supported inventories. Each button always sorts the inventory it is visually attached to. The configuration settings determine what additional inventories are affected.

| Button Target       | `sortPlayerInventory` | Sorted Inventories |
|---------------------|-----------------------|--------------------|
| Player Inventory    | `true` or `false`     | Player only        |
| Container Inventory | `false`               | Container only     |
| Container Inventory | `true`                | Container + Player |

The `sortHighlightedItem` setting has no effect when using sort buttons.

If `separateButton = false`, only one button is shown. When this button belongs to the container, it may sort both inventories depending on the above table.  
If `separateButton = true`, you will see one button for each inventory.

### Keybind

When using the sort keybind, behavior depends on what inventory (if any) is under your mouse.

| Mouse Over          | `sortPlayerInventory` | `sortHighlightedItem` | Sorted Inventories |
|---------------------|-----------------------|-----------------------|--------------------|
| Container Inventory | `false`               | `false`               | Container only     |
| Container Inventory | `true`                | `false`               | Container + Player |
| Container Inventory | `false`               | `true`                | Container only     |
| Container Inventory | `true`                | `true`                | Container + Player |
| Player Inventory    | `false`               | `false`               | Container only     |
| Player Inventory    | `true`                | `false`               | Container + Player |
| Player Inventory    | Any                   | `true`                | Player only        |
| Empty space         | `false`               | `false`               | Container only     |
| Empty space         | `true`                | `false`               | Container + Player |
| Empty space         | `false`               | `true`                | Container only     |
| Empty space         | `true`                | `true`                | Container + Player |

With the player-inventory screen open, or a menu whose container inventory is not sortable, the keybind targets the player inventory.
It sorts only if `allowPlayerInventorySorting` is on.
With no inventory screen open, the default `P` key opens the config menu instead of sorting.

### Double-Click

Double-clicking on an empty slot will attempt to sort the inventory, but only if `enableDoubleClickSort = true`.

Server double-click sorting follows the clicked empty slot; `sortHighlightedItem` does not change that target.

| Clicked Empty Slot  | `sortPlayerInventory` | Sorted Inventories |
|---------------------|-----------------------|--------------------|
| Container Inventory | `false`               | Container only     |
| Container Inventory | `true`                | Container + Player |
| Player Inventory    | Any                   | Player only        |

### Settings interactions

- If `separateButton = false` and `sortPlayerInventory = true`, pressing the container’s button will also sort the player inventory.
- `separateButton = false` hides the additional player button in container screens; it does not disable keybind, double-click, or command sorts.
- `enableDoubleClickSort = false` disables the double-click sorting trigger.
- `allowPlayerInventorySorting = false` blocks player sorts even when `sortPlayerInventory = true`, and hides player sort buttons.
- The `/invsort sort` and `/invsort sortme` commands work without the client mod. The player command respects the opt-out on supporting servers.



[server-installation]: /getting-started#server-installation
[configuration]: /configuration
