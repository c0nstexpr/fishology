pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}

rootProject.name = "fishology"
include(":core-api")
