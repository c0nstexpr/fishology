plugins {
    `kotlin-common`
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    modImplementation(libs.bundles.owo)
    annotationProcessor(libs.owo)

    listOf(libs.reaktive, libs.kermit).forEach(::api)
}

fabricProperty {
    put("fabricKotlin", libs.versions.fabric.kotlin)
    put("modmenu", libs.versions.modmenu)
    put("owo", libs.versions.owo)
}
