# Usage Guide

Inventory Sorter gives you multiple ways to quickly organize your inventory. Some features are available out of the box, while others depend on whether the mod is installed on the client and how it's configured.

## Sorting Methods

### Sort Button

If you are using the client mod and the sort button is enabled, a small icon will appear in supported inventories. 
Clicking it will instantly sort the contents of that container.

The button looks like this when unfocused: ![Sort Button (Unfocused)](https://raw.githubusercontent.com/KyrptonaughtMC/Inventory-Sorter/main/src/main/resources/assets/inventorysorter/textures/gui/button_unfocused.png)

The sort button must be enabled in the configuration to appear.

### Keybind

The client mod allows you to trigger sorting using a configurable keybind. 
By default, this key is unbound. You can assign a key from the Controls menu in Minecraft.

The keybind is only available when the client mod is installed and keybind support is enabled in the configuration.

### Middle-Click and Double-Click

You can sort an inventory by middle-clicking or double-clicking on an **empty slot**. 
This works even with vanilla clients, as long as the server has Inventory Sorter installed.

Middle-click and double-click sorting work in any inventory, including modded containers.

### Commands

Two commands are available to sort inventories directly:

- `/invsort sort` sorts the container you are currently looking at
- `/invsort sortme` sorts your player inventory

Commands work with both modded and vanilla clients. 
They are always available as long as the server has Inventory Sorter installed.

## Feature Availability

| Method            | Requires Client Mod | Works with Vanilla Client | Can be Disabled by Config |
|-------------------|---------------------|---------------------------|---------------------------|
| Sort Button       | Yes                 | No                        | Yes                       |
| Keybind           | Yes                 | No                        | Yes                       |
| Middle-Click      | No                  | Yes                       | Yes                       |
| Double-Click      | No                  | Yes                       | Yes                       |
| `/invsort sort`   | No                  | Yes                       | No                        |
| `/invsort sortme` | No                  | Yes                       | No                        |

If a sorting method doesn't seem to work, it may be disabled in the configuration. 
See the [Configuration](./04-configuration.md) section for more information.
