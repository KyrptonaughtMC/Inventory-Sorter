
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}
plugins {
    id("gg.meza.stonecraft") version "1.1.3"
    id("dev.kikugie.stonecutter") version "0.5"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            // Make the relevant version directories named "1.20.2-fabric", "1.20.2-forge", etc.
            for (it in loaders) vers("$version-$it", version)
        }

        mc("1.21.4", "fabric")
        mc("1.21.5", "fabric")

        vcsVersion = "1.21.5-fabric"
    }
    create(rootProject)
}

rootProject.name = "InventorySorter"
