plugins { `fabric-common` }

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    listOf(libs.owo, libs.bundles.fabric).forEach(::modApi)
    annotationProcessor(libs.owo)
    listOf(libs.bundles.reaktive, libs.bundles.kermit).forEach {
        api(it)
        include(it)
    }
}
