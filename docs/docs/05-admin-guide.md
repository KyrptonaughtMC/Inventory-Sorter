# Admin Guide

This guide is for server owners who want to control Inventory Sorter's behavior across all players.

Inventory Sorter's configuration file is shared between client and server, but on the server, its role is different: 
it defines compatibility rules that apply globally and cannot be overridden by individual players.

:::note
The `/invsort admin` commands do not exist on single-player worlds. Use the `/invsort` command equivalents instead.
:::

## Server Rules vs. Player Preferences

When installed on the server, Inventory Sorter reads `config/inventorysorter.json`, but it only pays attention to three settings:

```json
{
  "customCompatibilityListDownloadUrl": "",
  "preventSortForScreens": [],
  "hideButtonsForScreens": []
}
```

These values define which screens can be sorted and whether buttons should appear.  
All other values in the file are ignored on the server.

:::warning
Server rules are **authoritative**. If a screen is blocked by the server, players cannot bypass it even if they have the client mod installed.
:::

## Getting the screenID

If you ever need to find the screen ID of a container, you can use the `/invsort screenID` command.

## Preventing Sorting on Specific Screens

The `preventSortForScreens` list defines screens where **all sorting is disabled**, including:

- Sort button
- Keybind
- Double-click
- Commands

Useful for modded containers that break when sorted or for containers that need to preserve a specific item layout.
Each screen must be specified by its screen ID (not its display name). Example:

```
minecraft:generic_9x3
```

To manage this list in-game:

- `/invsort admin nosort add` — Add the screen associated with the block you’re looking at to the blocked list
- `/invsort admin nosort remove` — Remove the current screen associated with the block you’re looking at from the list
- `/invsort admin nosort list` — Show all currently blocked screens


## Hiding Sort Buttons for Specific Screens

The `hideButtonsForScreens` list hides the sort button for specific screens.  
This does **not** disable sorting via keybind, double-click, or command, it only removes the visible button.

Like `preventSortForScreens`, entries must be screen IDs. Example:

```
minecraft:shulker_box
```

This is useful when the button overlaps with other UI elements or if you want a cleaner interface for certain screens.

Players with the client mod can also hide buttons for themselves using [`CTRL+Click` on the sort button](/usage-guide#ctrlclick-to-hide).  
However, **server-defined entries override player preferences**.

To manage this list in-game:

- `/invsort admin hidebutton add` — Add the screen associated with the block you’re looking at to the blocked list
- `/invsort admin hidebutton remove` — Remove the current screen associated with the block you’re looking at from the list
- `/invsort admin hidebutton list` — Show all currently blocked screens

## Using a Remote Compatibility Config

Inventory Sorter supports fetching compatibility settings from a remote URL.

This is useful for:

- Server networks that want consistent config across all machines
- Friend groups or modpacks that want to share common screen rules

### How it works

When a URL is set via config or command, the mod fetches that JSON file on startup.  
Its values are merged **in memory** with the local config. It is **not saved to disk**.

Only `preventSortForScreens` and `hideButtonsForScreens` are merged.

### Example remote config file

```json
{
  "preventSortForScreens": [],
  "hideButtonsForScreens": [
    "minecraft:shulker_box"
  ]
}
```
To understand the expected JSON format, refer to the [default `inventorysorter.json`](/configuration#default-config-file) file and include only the two allowed lists.


### Usage

As a server operator, you mostly want to set a remote URL for the server. That ensures that all your players
get the same config, regardless of their client mod version.

#### On the server

These commands can be used both from the console of the server or in game.


- `/invsort admin remote set [url]` — Set the remote config source on the server
- `/invsort admin remote clear` — Remove the remote config from the server
- `/invsort admin remote show` — Display the currently configured URL on the server

#### On the client

:::tip
Use this if you want to share settings among your friends but you don't want to enforce those settings on the server.
:::

- `/invsort remote set [url]` — Set the remote config source on the client
- `/invsort remote clear` — Remove the remote config from the client
- `/invsort remote show` — Display the currently configured URL on the client

## Reloading the Server Config

If you made changes to the config via the commands, they should automatically take effect.
<br/>If you made changes to the config file directly, you need to reload the server config.

To do this, use the command:

```
/invsort admin reload
```

This reloads the server-side configuration and re-applies compatibility rules.  
You do **not** need to restart the server.

## Permissions and Access Control

All admin commands are organised behind the `/invsort admin` root command and require appropriate permissions.

For more detail, see the [Permissions Guide](/permissions).
