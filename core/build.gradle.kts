plugins {
    id("fabric.common")
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    listOf(libs.fabric.kotlin, libs.modmenu, libs.owo).forEach(::modApi)

    include(libs.owo.sentinel)
    annotationProcessor(libs.owo)

    listOf(libs.reaktive,libs.logging).forEach(::api)
}

fabricProperty {
    put("fabricKotlin", libs.versions.fabric.kotlin)
    put("modmenu", libs.versions.modmenu)
    put("owo", libs.versions.owo)
}
