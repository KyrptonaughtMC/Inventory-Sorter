# Moving active support to Minecraft 26.1 and newer

Minecraft 26.1 changes a lot for mod development.

For the first time, Minecraft: Java Edition is no longer obfuscated. Fabric has also moved away from Yarn as the officially supported mapping layer for 26.1 and newer versions. That makes 26.1 a clean break between the old modding world and the new one.

Because of that, I have decided to drop active support for Minecraft versions before 26.1.

Inventory Sorter will continue to exist for older Minecraft versions, and the existing releases are not going away. They will remain available for people who need them. What is changing is where new development happens.

From this point onward, new features will only be added to Minecraft 26.1 and newer.

This lets the project move forward without constantly carrying compatibility code for older, obfuscated versions of Minecraft. It also makes the current codebase much easier to work with. Less compatibility glue means less risk, less noise, and more room to improve the actual mod.

That matters because Inventory Sorter has a number of long-standing feature requests that have been difficult to justify while the project had to support such a wide range of Minecraft versions. With the codebase now focused on 26.1 and newer, those requests are much more realistic to revisit.

This change should also make the project healthier in the long run. The goal is not just to follow the latest Minecraft version for the sake of it. The goal is to keep Inventory Sorter maintainable, understandable, and pleasant to work on.

## What this means

Existing releases for older Minecraft versions will stay available.

Critical fixes for older versions may still happen if they are small, safe, and worth doing, but they are no longer the active development target.

New features will target Minecraft 26.1 and newer.

The codebase can now be cleaned up around the new Minecraft and Fabric ecosystem.

Long-standing feature requests may become practical again.

## Why now

The removal of obfuscation is a rare reset point for Minecraft modding. It changes the shape of the code, the toolchain, and the maintenance cost of supporting older versions.

Trying to keep one project moving cleanly across both sides of that break would make the mod harder to maintain for very little benefit. Drawing the line at 26.1 keeps the project focused and gives future work a much better foundation.

Inventory Sorter has always tried to stay lightweight and dependable. This is part of keeping it that way.
