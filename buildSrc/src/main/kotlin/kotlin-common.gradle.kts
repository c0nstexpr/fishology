import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode

plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
}

val modVersion: String by project
val modGroup: String by project

version = modVersion
group = modGroup

repositories {
    mavenCentral()
    mavenLocal()
}

val libs = versionCatalog

tasks {
    compileJava {
        sourceCompatibility = libs.versions["jvm"]
        targetCompatibility = sourceCompatibility
        options.encoding = Charsets.UTF_8.name()
    }

    compileKotlin {
        compilerOptions {
            jvmTargetValidationMode.set(JvmTargetValidationMode.ERROR)
            jvmTarget.set(JvmTarget.fromTarget(compileJava.get().targetCompatibility))
            allWarningsAsErrors = true
            languageVersion.set(KotlinVersion.fromVersion(libs.versions["kotlin"]))
        }
    }

    jar { from("LICENSE") }

    java { withSourcesJar() }
}
