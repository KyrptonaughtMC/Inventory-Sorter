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
}

sourceSets {
    test {
        java {
            srcDirs(rootProject.layout.projectDirectory.dir("src/test/java"))
        }
        resources {
            srcDirs(rootProject.layout.projectDirectory.dir("src/test/resources"))
        }
    }
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
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}"}
	}
}
