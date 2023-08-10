plugins { `fabric-common` }

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    fabricProject(":core", configurations.implementation.get())
    modRuntimeOnly(libs.modmenu)
}
