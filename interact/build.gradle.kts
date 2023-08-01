plugins {
    `kotlin-common`
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    fabricProject(":core")

    listOf(libs.fabric.kotlin, libs.owo, libs.modmenu).forEach(::modImplementation)
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fishology Client"
            isIdeConfigGenerated = true
        }
    }
}

fabricProperty {
    put("fabricKotlin", libs.versions.fabric.kotlin)
    put("modmenu", libs.versions.modmenu)
    put("owo", libs.versions.owo)
}