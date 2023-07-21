plugins {
    id("fabric.common")
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    modApi(libs.fabric.kotlin)

    modApi(libs.modmenu)

    modApi(libs.owo)
    include(libs.owo.sentinel)
    annotationProcessor(libs.owo)

    api(libs.reaktive)
    implementation(libs.logging)
}

fabricProperty {
    put("fabricKotlin", libs.versions.fabric.kotlin)
    put("modmenu", libs.versions.modmenu)
    put("owo", libs.versions.owo)
}
