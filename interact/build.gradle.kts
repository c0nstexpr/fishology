plugins { `fabric-common` }

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

val projConfig: Configuration by configurations.creating

configurations { modApi { extendsFrom(projConfig) } }

dependencies {
    fabricProject(":core", projConfig)
    modImplementation(libs.owo)
    modRuntimeOnly(libs.modmenu)
}
