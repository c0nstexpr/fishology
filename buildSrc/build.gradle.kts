import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
        libs.plugins.kotlin.jvm, libs.plugins.loom, libs.plugins.spotless
    ).forEach { provider ->
        val p = provider.get()
        val id = p.pluginId
        val version = p.version

        implementation("$id:$id.gradle.plugin:$version")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:latest.release")
}

tasks { compileJava { options.encoding = Charsets.UTF_8.name() } }

inline fun <reified T : Enum<T>> maxEnumValue() = enumValues<T>().maxOrNull()!!