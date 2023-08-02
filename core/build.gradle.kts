plugins {
    `kotlin-common`
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    listOf(libs.fabric.kotlin, libs.owo, libs.modmenu).forEach(::modImplementation)
    annotationProcessor(libs.owo)

    listOf(libs.reaktive, libs.kermit).forEach(::api)
}

fabricProperty {
    put("fabricKotlin", libs.versions.fabric.kotlin)
    put("modmenu", libs.versions.modmenu)
    put("owo", libs.versions.owo)
}
