---
slug: /
sidebar_position: 1
---

# Introduction

Inventory Sorter is a lightweight mod that helps keep inventories organized with minimal effort.

It is designed to run server-side first. This allows it to work even with vanilla clients, making it suitable for multiplayer servers, modpacks, and single-player worlds. It supports all inventories, including those added by other mods.

## Server-side-first design

Unlike most sorting mods, Inventory Sorter performs sorting on the server. The client sends a request, and the server applies the sort. This avoids simulating manual item movement from the client.

This approach enables:

- Sorting with unmodded clients (via commands, middle click, or double click)
- Cleaner networking in multiplayer
- Simplified deployment for modpacks and server operators

Client-side installation is optional. Without the client mod, users will not see a sort button, keybind, or config menu. These features are purely visual and do not affect core functionality.

There is an ongoing effort to support client-only usage, but this is not currently available.

## Differences from other sorting mods

- Server-side functionality by default
- Works with vanilla clients
- Fully configurable
- Focused feature set with minimal overhead

## Project status

The mod was originally developed by [Kyrptonaught](http://github.com/kyrptonaught). 
As of Minecraft 1.21.5, it is under active maintenance with a focus on compatibility, 
documentation, and long-term support by [Meza](https://github.com/meza).

---

### Suggested Documentation Structure

#### Introduction
- What is Inventory Sorter?
- Why server-side-first?
- How is it different from other sorting mods?

#### Getting Started
- Installation (client + server breakdown)
- Dependencies (Cloth Config, Mod Menu)
- Supported Versions
- Quick Start (join a server, press the key, done)

#### Usage Guide
- How to sort
- Sort button
- Keybind
- Middle click
- Double click
- Commands - sort, sortme - where do we put the others that relate to compatibility and config?
- When each method works (with/without client mod)

#### Configuration
- Accessing the config (client vs server)
- Sorting behavior (sorting modes? rules?)
  - sort types (name, category, id, mod) - what do all these mean?
- Compatibility filters (blocklist certain containers)
  - hide sort button vs. prevent sorting
  - how can mod developers add to this?
  - how can individual users add to this?
- Visuals (toggle sort buttons, etc.) - how to override it with resource packs

#### Compatibility
- Works with modded inventories
- Known issues / exceptions
- How to host your own compatibility list
- How to fix sorting problems with specific mods

#### Commands
- `/invsort sort`
- `/invsort sortme`
- `/invsort sortType [type]`
- `/invsort hidebutton`
- `/invsort preventsort`
- `/invsort doubleClickSort on`
- `/invsort doubleClickSort off`
- `/invsort middleClickSort on`
- `/invsort middleClickSort off`
- `/invsort downloadCompatibilityList [URL]`

#### FAQ
- Why doesn’t sorting work on my server?
- Do I need the client mod?
- Can I disable the button in certain containers?
- Does it work in creative mode?

#### For Modpack Developers
- Server-only install
- Controlling defaults
- Disabling features via config
