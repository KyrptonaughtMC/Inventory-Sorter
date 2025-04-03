# Getting Started

Inventory Sorter works with most modern Minecraft versions and is easy to set up for both multiplayer servers and single-player worlds.

The mod currently supports the Fabric mod loader. Forge and NeoForge support is planned but not yet available.

## Installation Overview

Installation requirements depend on how you plan to use the mod.

### Server Installation

If you are running a Fabric-based Minecraft server:

- Install [Fabric API][fabric-api]
- Install [Inventory Sorter][inventory-sorter]

No additional configuration is needed. The mod will automatically handle sort requests from clients.

### Client Installation

If you want to add client-side features like keybinds, configuration menus, or sort buttons:

- Install [Fabric API][fabric-api]
- Install [Inventory Sorter][inventory-sorter]
- Install [Cloth Config][cloth-config] (required for config menus)
- (Optional) Install [Mod Menu][mod-menu] for easier in-game access to settings

If the server already has Inventory Sorter installed, you’ll get additional functionality by installing it client-side too.

### Single-Player Installation

Single-player behaves like a local server, so both server-side and client-side mods are required.

Install the following:

- [Fabric API][fabric-api]
- [Inventory Sorter][inventory-sorter]
- [Cloth Config][cloth-config]
- (Optional) [Mod Menu][mod-menu]

## Supported Versions

Inventory Sorter supports Minecraft versions from **1.14.2** onward.

Active development and support focus on **1.21.4+**. Bugfixes or new features will only be released for these newer versions.

## Quick Start

Once installed:

1. Launch the game and open any inventory. Your player inventory works fine.
2. Try middle-clicking or double-clicking on an empty slot.
3. If Inventory Sorter is installed correctly, the inventory will instantly re-arrange.

This works even with vanilla clients when connected to a properly configured server.

To access more features, such as the sort button or keybinds, install the mod on the client as well.

[fabric-api]: https://modrinth.com/mod/fabric-api
[inventory-sorter]: https://modrinth.com/mod/inventory-sorting
[cloth-config]: https://modrinth.com/mod/cloth-config
[mod-menu]: https://modrinth.com/mod/modmenu
