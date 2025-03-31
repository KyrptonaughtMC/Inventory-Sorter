import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

modSettings {
    clientOptions {
        darkBackground = true
        musicVolume = 0.0
        narrator = false
    }

    variableReplacements = mapOf(
        "schema" to "\$schema",
    )
}

repositories {
	maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.shedaniel.me")
}

dependencies {
    modImplementation("com.google.code.gson:gson:2.+")
    modApi("com.terraformersmc:modmenu:${mod.prop("modmenu_version")}")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${mod.prop("cloth_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modImplementation("com.github.erosb:everit-json-schema:1.14.4")

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
