# Getting Started

Inventory Sorter works with most modern Minecraft versions and is easy to set up for both multiplayer servers and single-player worlds.

The mod currently supports the Fabric mod loader. Forge and NeoForge support is planned but not yet available.

## Installation Overview

Installation requirements depend on how you plan to use the mod.

### Server Installation

If you are running a Fabric-based Minecraft server:

- Install [Fabric API][fabric-api]
- Install [Inventory Sorter][inventory-sorter]

No additional configuration is needed. The mod automatically handles sort requests from clients.

### Client Installation

If you want to add client-side features like keybinds, configuration menus, or sort buttons:

- Install [Fabric API][fabric-api]
- Install [Inventory Sorter][inventory-sorter]
- Install [Cloth Config][cloth-config] (required for config menus)
- (Optional) Install [Mod Menu][mod-menu] for easier in-game access to settings

:::tip
If you're only joining a server, and it has Inventory Sorter installed, you don't *need* it client-side, but installing it adds GUI buttons and keybinds for sorting, which most players prefer.
:::

### Single-Player Installation

Single-player runs an integrated server under the hood, so you only need to install the **client-side mods**.

Follow the same steps as in [Client Installation](#client-installation):


## Supported Versions

Inventory Sorter supports Minecraft versions from **1.14.2** onward.

Active development and support focus on **1.21.4+**.
Only Minecraft 1.21.4+ will receive bugfixes or new features.

## Quick Start

Once installed:

1. Launch the game and open any inventory. Your player inventory works fine.
2. Try double-clicking on an empty slot.
3. If Inventory Sorter is installed correctly, the inventory will instantly re-arrange.

This works even with vanilla clients when connected to a properly configured server.

To access more features, such as the sort button or keybinds, install the mod on the client as well.

[fabric-api]: https://modrinth.com/mod/fabric-api
[inventory-sorter]: https://modrinth.com/mod/inventory-sorting
[cloth-config]: https://modrinth.com/mod/cloth-config
[mod-menu]: https://modrinth.com/mod/modmenu
