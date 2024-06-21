plugins { `fabric-common` }

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    modApi(libs.owo)
    annotationProcessor(libs.owo)
    listOf(libs.bundles.reaktive, libs.bundles.kermit).forEach {
        api(it)
        include(it)
    }
    modClientRuntimeOnly(libs.modmenu)
}
