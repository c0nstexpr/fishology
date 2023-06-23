import com.modrinth.minotaur.TaskModrinthUpload

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.lint)

    alias(libs.plugins.modrinth.minotaur)
    alias(libs.plugins.loom)
    `maven-publish`
}

val modVersion: String by project
val modId: String by project
val modName: String by project

version = modVersion
group = "org.c0nstexpr"

repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    mavenLocal()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn.mappings)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)
}

tasks {
    val javaVersion = JavaVersion.VERSION_17

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
                    "fabricKotlin" to libs.versions.fabric.kotlin.get()
                )
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            kotlinOptions.allWarningsAsErrors = true
        }
    }

    jar {
        from("LICENSE")
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    withType<TaskModrinthUpload> {
        onlyIf { System.getenv().contains("MODRINTH_TOKEN") }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
