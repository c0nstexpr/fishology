plugins {
    `kotlin-common`
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    fabricProject(":core", findProject = ::findProject)
    modImplementation(libs.owo)
    modRuntimeOnly(libs.modmenu)
}
