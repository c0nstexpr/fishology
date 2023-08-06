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

    modImplementation(libs.owo)
    modRuntimeOnly(libs.modmenu)
}
