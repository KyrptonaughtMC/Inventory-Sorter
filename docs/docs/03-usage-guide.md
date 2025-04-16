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
This can be toggled in the configuration screen or directly in the config file.

![](/img/usage-guide/player-inventory-button-config.png)

### Hiding the main Sort Button

Clicking the sort button while holding the **[modifier key](#modifier-key-for-hidingswitching)** (`CTRL` by default) will hide the button for the current screen.
This is useful if the button overlaps with other UI elements or mods.

To re-enable it, use the GUI configuration menu or manually update the config file.

### Scroll to Change Sort Type

While hovering over the sort button, scrolling the mouse wheel will cycle through the available sorting methods:
**Category**, **Mod**, **Name**, and **Id**.

![](/img/usage-guide/scroll-type.webp)

By default, you can scroll through the sort types without holding the **[modifier key](#modifier-key-for-hidingswitching)** (`CTRL` by default).
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

#### Modifier Key (for hiding/switching)

A key to hold to uncover alternative functionalities of the sort button. 
- Hide the sort button
- Switch between sort types by scrolling.

#### Sort inventory

Sorts your inventory when inside a container.

#### Open Config 

Opens the Inventory Sorter settings when no container is open.  
By default, the **Sort inventory** key (`P`) also opens the config screen when pressed **outside of any container**.

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
- `/invsort sortMe` sorts your player inventory

Commands work with both modded and vanilla clients.
They are always available as long as the server has [Inventory Sorter installed][server-installation].

---

## Feature Availability

| Feature           | Requires Client Mod | Works with Vanilla Client | Configurable |
|-------------------|---------------------|---------------------------|--------------|
| Sort Button       | ✅                   | ❌                         | ✅            |
| Keybind           | ✅                   | ❌                         | ✅            |
| Config Menu       | ✅                   | ❌                         | ✅            |
| Double-Click      | ❌                   | ✅                         | ✅            |
| `/invsort sort`   | ❌                   | ✅                         | ❌            |
| `/invsort sortMe` | ❌                   | ✅                         | ❌            |

If a feature doesn't seem to work, it may be disabled in the configuration.
See the [Configuration][configuration] section for more information.

---

## Sorting Behavior Reference

Inventory Sorter offers multiple ways to sort your inventory. The behavior depends on which method you use and how the following settings are configured:

- `sortPlayerInventory`: Whether sorting actions also affect your player inventory.
- `sortHighlightedItem`: Whether sorting is limited to the inventory under your mouse cursor.
- `enableDoubleClickSort`: Whether double-clicking an empty slot triggers sorting.

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
| Container Inventory | Any                   | `true`                | Container only     |
| Player Inventory    | `false`               | `false`               | Container + Player |
| Player Inventory    | `true`                | `false`               | Container + Player |
| Player Inventory    | Any                   | `true`                | Player only        |
| Empty space         | `false`               | `false`               | Container only     |
| Empty space         | `true`                | `false`               | Container + Player |
| Empty space         | Any                   | `true`                | Container + Player |

If no container is open and the keybind is used, the player inventory is always sorted.

### Double-Click

Double-clicking on an empty slot will attempt to sort the inventory, but only if `enableDoubleClickSort = true`.

| Mouse Over          | `sortPlayerInventory` | `sortHighlightedItem` | Sorted Inventories         |
|---------------------|-----------------------|-----------------------|----------------------------|
| Container Inventory | `false`               | `false`               | Container only             |
| Container Inventory | `true`                | `false`               | Container + Player         |
| Container Inventory | Any                   | `true`                | Container only             |
| Player Inventory    | `false`               | `false`               | Container + Player         |
| Player Inventory    | `true`                | `false`               | Container + Player         |
| Player Inventory    | Any                   | `true`                | Player only                |

### Known UX Edge Cases

There are a few common situations where user intent may not match what actually happens.

- If `separateButton = false` and `sortPlayerInventory = true`, pressing the container’s button will also sort the player inventory.
- If `separateButton = false` and `sortPlayerInventory = false`, there is no visible way to sort the player inventory unless a keybind is pressed.
- If `enableDoubleClickSort = false`, double-clicking will never trigger sorting, even if `sortHighlightedItem = true`.
- The sort keybind is always available when the client mod is installed. It acts as a fallback to manually sort the player inventory if no other method is enabled.
- The `/invsort sort` and `/invsort sortme` commands are always available, regardless of the client mod. They can be used to sort the player inventory or any targeted container.



[server-installation]: /getting-started#server-installation
[configuration]: /configuration
