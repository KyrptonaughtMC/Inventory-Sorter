import gg.meza.stonecraft.mod
import java.io.File

plugins {
    id("gg.meza.stonecraft")
    jacoco
}

stonecutter {
    constants["hasModMenu"] = mod.prop("modmenu_version", "0") != "0"

    replacements.string(project.mod.isNeoforge) {
        replace("player.clientInformation().language()", "player.getLanguage()")
    }

    replacements.string(stonecutter.current.parsed < "26.2") {
        replace("Minecraft.getInstance().gui.screen()", "Minecraft.getInstance().screen")
        replace("minecraft.gui.screen()", "minecraft.screen")
        replace("DYED_SHULKER_BOX.white()", "WHITE_SHULKER_BOX")
        replace("DYED_SHULKER_BOX.purple()", "PURPLE_SHULKER_BOX")
    }
}

modSettings {

    clientOptions {
        darkBackground = true
        musicVolume = 0.0
        narrator = false
        fov = 90
    }

    variableReplacements =
        mapOf(
            "schema" to "\$schema",
            "clothVersion" to mod.prop("cloth_version"),
            "modmenuVersion" to mod.prop("modmenu_version", "*"),
            "fabricPermissionsApiVersion" to mod.prop("fabric_permissions_api_version"),
            "fabricVersion" to mod.prop("fabric_version"),
            "minecraftVersionVirtual" to mod.prop("minecraft_version_virtual", stonecutter.current.version),
        )
}

repositories {
    mavenLocal()
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.shedaniel.me")
    maven("https://maven.meza.gg/releases")
    maven("https://maven.meza.gg/snapshots")
    maven("https://maven.nucleoid.xyz")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    implementation("com.github.erosb:everit-json-schema:1.14.4")
    include("com.github.erosb:everit-json-schema:1.14.4")
    include("org.json:json:20231013")

    if (mod.isFabric) {
        implementation("me.lucko:fabric-permissions-api:${mod.prop("fabric_permissions_api_version")}")
        include("me.lucko:fabric-permissions-api:${mod.prop("fabric_permissions_api_version")}")
    }

    implementation("gg.meza:meza_core-${mod.loader}:${mod.prop("meza_core_version")}+${stonecutter.current.version}")
    include("gg.meza:meza_core-${mod.loader}:${mod.prop("meza_core_version")}+${stonecutter.current.version}")

    compileOnly("maven.modrinth:trinkets-updated:${mod.prop("trinkets_version")}")
    localRuntime("maven.modrinth:trinkets-updated:${mod.prop("trinkets_version")}")
    testCompileOnly("maven.modrinth:trinkets-updated:${mod.prop("trinkets_version")}")
    testRuntimeOnly("maven.modrinth:trinkets-updated:${mod.prop("trinkets_version")}")

    if (mod.isFabric) {
        try {
            api("com.terraformersmc:modmenu:${mod.prop("modmenu_version")}")
        } catch (_: Exception) {
            logger.warn("Modmenu not found, skipping dependency.")
        }
    }
    api("me.shedaniel.cloth:cloth-config-${mod.loader}:${mod.prop("cloth_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    if (mod.isFabric) {
        testImplementation("net.fabricmc:fabric-loader-junit:${mod.prop("loader_version")}")
    } else {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly("net.neoforged.fancymodloader:junit-fml:11.0.13")
    }
    testImplementation("com.google.jimfs:jimfs:1.1")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("**/e2e/**")
                }
            },
        ),
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
    exclude("**/e2e/**")
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
    if (project.mod.isNeoforge) {
        type.set(BETA)
    }

    modrinth {
        if (mod.isFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
        requires("cloth-config")
    }

    curseforge {
        client = true
        server = true

        if (mod.isFabric) {
            requires("fabric-api")
            optional("modmenu")
        }
        requires("cloth-config")
    }
}
