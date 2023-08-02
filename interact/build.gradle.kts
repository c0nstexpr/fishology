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

    listOf(libs.owo, libs.fabric.kotlin, libs.modmenu).forEach(::modImplementation)
}

fabricProperty {
    put("fabricKotlin", libs.versions.fabric.kotlin)
    put("owo", libs.versions.owo)
}
