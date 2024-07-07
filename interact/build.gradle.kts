plugins {
    `fabric-common`
    alias(libs.plugins.minotaur)
}

val modId: String by project

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

dependencies {
    implementation(project(":core", "namedElements"))
    include(project(":core"))

    modApi(libs.owo)
    modClientRuntimeOnly(libs.modmenu)
}

loom {
    accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
}

tasks {
    remapJar { archiveClassifier.set("") }

    this.modrinth { dependsOn(remapJar, modrinthSyncBody) }

    processResources {
        doFirst {
            fabricProperty {
                put("owo", libs.versions.owo)
                put("modmenu", libs.versions.modmenu)
                put("fabricKotlin", libs.versions.fabric.kotlin)
            }
        }
    }
}

System.getenv().getOrDefault("MODRINTH_TOKEN", null)?.let {
    modrinth {
        projectId.set("rjuXQb7H")
        token.set(it)
        versionNumber.set(version.toString())
        versionType.set("beta")
        uploadFile.set(tasks.remapJar.get())
        loaders.add("fabric")
        syncBodyFrom.set(rootProject.file("README.md").readText())
    }
}
