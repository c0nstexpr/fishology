import org.jetbrains.kotlin.config.LanguageVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)

    alias(libs.plugins.minotaur)
    alias(libs.plugins.loom)
}

val modVersion: String by project
val modId: String by project
val modName: String by project

version = modVersion
group = "org.c0nstexpr"
base.archivesName.set(modId)

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://maven.fabricmc.net/")
    maven("https://maven.wispforest.io")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn.mappings)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    modImplementation(libs.owo)
    annotationProcessor(libs.owo)
    include(libs.owo.sentinel)

    implementation(libs.reaktive)
}

loom {
    accessWidenerPath.set(file("src/main/resources/${modId}.accesswidener"))
}

spotless {
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
    }

    kotlin {
        ktlint()
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()

        sourceCompatibility = libs.versions.jvm.get()
        targetCompatibility = libs.versions.jvm.get()

        sourceSets.main.get().java.srcDirs(options.generatedSourceOutputDirectory.get())
    }

    compileKotlin {
        kotlinOptions {
            allWarningsAsErrors = true
            languageVersion = LanguageVersion.KOTLIN_1_8.versionString
            jvmTarget = libs.versions.jvm.get()
        }
    }

    processResources {
        inputs.property("id", modId)
        inputs.property("name", modName)
        inputs.property("version", version)

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "id" to modId,
                    "version" to version,
                    "name" to modName,
                    "minecraft" to libs.versions.minecraft.get(),
                    "fabricApi" to libs.versions.fabric.api.get(),
                    "fabricLoader" to libs.versions.fabric.loader.get(),
                    "fabricKotlin" to libs.versions.fabric.kotlin.get(),
                    "owo" to libs.versions.owo.get()
                )
            )
        }
    }

    jar {
        from("LICENSE")
    }

    java {
        withSourcesJar()
    }
}

if (System.getenv().contains("MODRINTH_TOKEN")) modrinth {}
