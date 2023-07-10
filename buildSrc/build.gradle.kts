import org.jetbrains.kotlin.config.LanguageVersion

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()

    maven("https://maven.fabricmc.net/")
}

dependencies {
    fun plugin(provider: Provider<PluginDependency>): String {
        val p = provider.get()
        val id = p.pluginId
        val version = p.version

        return "${id}:${id}.gradle.plugin:${version}"
    }

    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation(plugin(libs.plugins.loom))
    implementation(plugin(libs.plugins.minotaur))
    implementation(plugin(libs.plugins.spotless))

    // TODO: workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks {
    compileKotlin {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = libs.versions.jvm.get()
        }
    }
}
