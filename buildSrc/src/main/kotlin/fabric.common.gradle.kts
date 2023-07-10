import org.gradle.accessors.dm.LibrariesForLibs

val modVersion: String by project
val modId: String by project
val modName: String by project

version = modVersion
group = "org.c0nstexpr"

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("fabric-loom")
    id("com.modrinth.minotaur")
    id("com.diffplug.spotless")
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://maven.fabricmc.net/")
    maven("https://maven.wispforest.io")
}

val Project.libs get() = project.extensions.getByName("libs") as LibrariesForLibs

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn.mappings)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    modImplementation(libs.owo)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()

        sourceCompatibility = libs.versions.jvm.get()
        targetCompatibility = libs.versions.jvm.get()
    }

    compileKotlin {
        kotlinOptions {
            allWarningsAsErrors = true
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

    if (System.getenv().contains("MODRINTH_TOKEN")) modrinth {}
}

spotless {
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
    }

    kotlin { ktlint() }
}
