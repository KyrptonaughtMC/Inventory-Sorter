# Permissions

Inventory Sorter includes built-in support for [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api).  
If no permissions manager (like [LuckPerms](https://modrinth.com/plugin/luckperms)) is present, all permission checks fall back to vanilla op levels.

:::info
If you have never used permissions before, check the LuckPerms wiki for a full guide on what they are 
and how to use them: [LuckPerms Wiki](https://luckperms.net/wiki/Usage)
:::

Each permission node listed below can be overridden individually.  
Permissions default to their corresponding vanilla level when not explicitly set.

### Player Permissions

These grant players access to the mod's commands and features.

The default **permission level is 0**, which means that all players can use them.

| Permission Node                                    | Command                                    |
|----------------------------------------------------|--------------------------------------------|
| `inventorysorter.command.sort`                     | `/invsort sort`                            |
| `inventorysorter.command.sortme`                   | `/invsort sortMe`                          |
| `inventorysorter.command.sorttype`                 | `/invsort sortType`                        |
| `inventorysorter.command.sortplayerinventory`      | `/invsort sortPlayerInventory on/off`      |
| `inventorysorter.command.sorthighlightedinventory` | `/invsort sortHighlightedInventory on/off` |
| `inventorysorter.command.nosort`                   | `/invsort nosort add/remove/list`          |
| `inventorysorter.command.middleclicksort`          | `/invsort middleClickSort on/off`          |
| `inventorysorter.command.doubleclicksort`          | `/invsort doubleClickSort on/off`          |
| `inventorysorter.command.reload`                   | `/invsort reload`                          |
| `inventorysorter.command.screenid`                 | `/invsort screenID`                        |


### Admin Permissions

These grant control over server-side settings and features.

The default **permission level is 2**, which means that only server operators can use them.

| Permission Node                                   | Command                            |
|---------------------------------------------------|------------------------------------|
| `inventorysorter.command.admin`                   | `/invsort admin *`                 |
| `inventorysorter.command.admin.reload`            | `/invsort admin reload`            |
| `inventorysorter.command.admin.nosort`            | `/invsort admin nosort *`          |
| `inventorysorter.command.admin.nosort.add`        | `/invsort admin nosort add`        |
| `inventorysorter.command.admin.nosort.remove`     | `/invsort admin nosort remove`     |
| `inventorysorter.command.admin.nosort.list`       | `/invsort admin nosort list`       |
| `inventorysorter.command.admin.hidebutton`        | `/invsort admin hidebutton *`      |
| `inventorysorter.command.admin.hidebutton.add`    | `/invsort admin hidebutton add`    |
| `inventorysorter.command.admin.hidebutton.remove` | `/invsort admin hidebutton remove` |
| `inventorysorter.command.admin.hidebutton.list`   | `/invsort admin hidebutton list`   |
| `inventorysorter.command.admin.remote`            | `/invsort admin remote *`          |
| `inventorysorter.command.admin.remote.set`        | `/invsort admin remote set [url]`  |
| `inventorysorter.command.admin.remote.clear`      | `/invsort admin remote clear`      |
| `inventorysorter.command.admin.remote.show`       | `/invsort admin remote show`       |
