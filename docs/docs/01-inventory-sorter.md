---
slug: /
sidebar_position: 1
---

# Introduction

Inventory Sorter is a lightweight mod that helps keep inventories organized with minimal effort.

It is designed to run server-side first. This allows it to work even with vanilla clients, making it suitable for multiplayer servers, modpacks, and single-player worlds. It supports all inventories, including those added by other mods.

## Server-side-first design

Unlike most sorting mods, Inventory Sorter performs sorting on the server. 
The client sends a request, and the server applies the sort. 
This avoids simulating manual item movement from the client.

This approach enables:

- Sorting with unmodded clients (via commands or double click)
- Cleaner networking in multiplayer
- Simplified deployment for modpacks and server operators

Client-side installation is optional. Without the client mod, users will not see a sort button, keybind, or config menu. These features are purely visual and do not affect core functionality.

## Server-only support

The mod can be installed on a server without requiring clients to install it. Players can sort their inventories as described in the [usage guide](/usage-guide).

## Client-Only support

Inventory Sorter can also be installed on the client without a server installation. This however spams the UI with a lot of actions and can cause issues when synchronizing with the server. It is not recommended to use the client mod without a server installation, but it is possible.

## Differences from other sorting mods

- Server-side functionality by default
- Works with vanilla clients
- Fully configurable
- Focused feature set with minimal overhead

## Compatible Mods

- Trinkets: bundles equipped in Trinkets slots can be used when sorting the player inventory.


## Project status

The mod was originally developed by [Kyrptonaught](http://github.com/kyrptonaught). 
As of Minecraft 1.21.5, it is under active maintenance with a focus on compatibility, 
documentation, and long-term support by [Meza](https://github.com/meza).

---

Ready to install? Check the [Getting Started guide](/getting-started).

## Credits

Icon and logo artwork by [Annarooma](https://www.twitch.tv/annarooma).  
Thank you for the gorgeous design!

---

Thanks to all the patrons who make this project possible!
<!-- marker:patrons-start -->



<!-- marker:patrons-end -->
