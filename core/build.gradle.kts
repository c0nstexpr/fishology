plugins {
    `kotlin-common`
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

val includeAndExpose: Configuration by configurations.creating

configurations {
    modApi { extendsFrom(includeAndExpose) }
    include { extendsFrom(includeAndExpose) }
}

dependencies {
    modImplementation(libs.bundles.owo)
    annotationProcessor(libs.owo)
    listOf(libs.reaktive, libs.kermit).forEach {
        includeAndExpose(it)
        implementation(it)
        shadow(it)
    }
}

fabricProperty {
    put("owo", libs.versions.owo)
}
