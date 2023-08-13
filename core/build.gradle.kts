plugins { `fabric-common` }

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    modImplementation(libs.owo)
    annotationProcessor(libs.owo)
    modRuntimeOnly(libs.modmenu)
    listOf(libs.reaktive, libs.kermit).forEach { fabricLibrary(it, tasks) }
}

fabricProperty {
    put("owo", libs.versions.owo)
}
