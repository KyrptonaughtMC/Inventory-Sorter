#!/bin/sh

VERSION=$1

echo "Replacing version with ${VERSION}"
sed -e "s/0.0-SNAPSHOT/${VERSION}/" -i gradle.properties
sed -e "s/VERSION_REPL/${VERSION}/" -i src/main/java/net/kyrptonaught/inventorysorter/InventorySorterMod.java


./gradlew chiseledTest chiseledGameTest chiseledBuildAndCollect chiseledPublishMods --stacktrace
