import org.jetbrains.kotlin.config.LanguageVersion

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven("https://maven.fabricmc.net/")
}

dependencies {
    listOf(
        libs.plugins.kotlin.jvm,
        libs.plugins.loom,
        libs.plugins.minotaur,
        libs.plugins.spotless,
        libs.plugins.vineflower,
        libs.plugins.shadow
    ).forEach { provider ->
        val p = provider.get()
        val id = p.pluginId
        val version = p.version

        implementation("$id:$id.gradle.plugin:$version")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:latest.release")
}

tasks {
    compileJava {
        sourceCompatibility = libs.versions.jvm.get()
        targetCompatibility = sourceCompatibility
        options.encoding = Charsets.UTF_8.name()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = compileJava.get().targetCompatibility
            allWarningsAsErrors = true
            languageVersion = LanguageVersion.LATEST_STABLE.versionString
            apiVersion = languageVersion
        }
    }
}
