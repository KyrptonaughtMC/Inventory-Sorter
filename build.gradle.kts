import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

stonecutter.replacement(true, "MOD_VERSION_REPL", mod.version)

modSettings {

    clientOptions {
        darkBackground = true
        musicVolume = 0.0
        narrator = false
    }

    variableReplacements = mapOf(
        "schema" to "\$schema",
        "clothVersion" to mod.prop("cloth_version"),
        "modmenuVersion" to mod.prop("modmenu_version"),
        "fabricPermissionsApiVersion" to mod.prop("fabric_permissions_api_version"),
        "fabricVersion" to mod.prop("fabric_version")
    )
}

repositories {
    mavenLocal()
	maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.shedaniel.me")
    maven("https://maven.meza.gg/releases")
    maven("https://maven.nucleoid.xyz")
}

dependencies {
    modImplementation("com.github.erosb:everit-json-schema:1.14.4")
    include("com.github.erosb:everit-json-schema:1.14.4")
    include("org.json:json:20231013")

    modImplementation("me.lucko:fabric-permissions-api:${mod.prop("fabric_permissions_api_version")}")
    include("me.lucko:fabric-permissions-api:${mod.prop("fabric_permissions_api_version")}")

    modImplementation("gg.meza:meza_core-${mod.loader}:${mod.prop("meza_core_version")}")
    include("gg.meza:meza_core-${mod.loader}:${mod.prop("meza_core_version")}")

    modImplementation("xyz.nucleoid:server-translations-api:${mod.prop("server_translations_api_version")}")
    include("xyz.nucleoid:server-translations-api:${mod.prop("server_translations_api_version")}")

    modApi("com.terraformersmc:modmenu:${mod.prop("modmenu_version")}")
    modApi("me.shedaniel.cloth:cloth-config-${mod.loader}:${mod.prop("cloth_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    testImplementation("net.fabricmc:fabric-loader-junit:${mod.prop("loader_version")}")
    testImplementation("com.google.jimfs:jimfs:1.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}"}
	}
}

tasks.processResources {
    doLast {
        val resourcesDir = project.layout.buildDirectory.dir("resources/main")
        val srcDir = resourcesDir.get().dir("assets/${mod.id}/lang")
        val destDir = resourcesDir.get().dir("data/${mod.id}/lang")

        if (srcDir.asFile.exists()) {
            destDir.asFile.mkdirs()
            copy {
                from(srcDir)
                into(destDir)
                rename { filename -> filename.lowercase() }
            }
            logger.info("Copied language files from assets/${mod.id}/lang to data/${mod.id}/lang")
        } else {
            logger.error("Source language directory not found: ${srcDir.asFile.absolutePath}")
        }
    }
}

publishMods {
    modrinth {
        if (mod.isFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
        requires("cloth-config")
    }

    curseforge {
        clientRequired = false
        serverRequired = true
        if (mod.isFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
        requires("cloth-config")
    }
}
