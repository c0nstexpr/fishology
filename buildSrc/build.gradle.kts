import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "latest.release"
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()

    maven("https://maven.fabricmc.net/")
}

dependencies {
    fun DependencyHandler.implementation(provider: Provider<PluginDependency>): Dependency? {
        val p = provider.get()
        val id = p.pluginId
        val version = p.version

        return implementation("$id:$id.gradle.plugin:$version")
    }

    implementation(libs.plugins.kotlin.jvm)
    implementation(libs.plugins.loom)
    implementation(libs.plugins.minotaur)
    implementation(libs.plugins.spotless)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:latest.release")
}

tasks {
    compileJava{
        targetCompatibility = JvmTarget.values().last().target
    }

    compileKotlin {
        kotlinOptions {
            languageVersion = LanguageVersion.LATEST_STABLE.versionString
            apiVersion = languageVersion
            allWarningsAsErrors = true
            jvmTarget = compileJava.get().targetCompatibility
        }
    }
}
