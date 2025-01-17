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

repositories {
	maven("https://maven.kyrptonaught.dev")
	maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    modImplementation("net.kyrptonaught:kyrptconfig:1.6.1-1.21.4") {
        exclude(module = "modmenu")
        exclude(group = "net.fabricmc.fabric-api")
    }
    modImplementation("com.terraformersmc:modmenu:13.0.0")
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}"}
	}
}
