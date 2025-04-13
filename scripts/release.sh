#!/bin/sh

VERSION=$1

echo "Replacing version with ${VERSION}"
sed -e "s/0.0-SNAPSHOT/${VERSION}/" -i gradle.properties

./gradlew chiseledGameTest chiseledBuildAndCollect chiseledPublishMods --stacktrace
