plugins {
    `fabric-common`
    alias(libs.plugins.minotaur)
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

base { archivesName.set("fishology") }

dependencies {
    project(":core", "namedElements").let {
        api(it)
        include(it)
        clientImplementation(it.dependencyProject.srcClient.output)
    }
    listOf(libs.owo, libs.bundles.fabric).forEach(::modApi)
    modRuntimeOnly(libs.modmenu)
}

tasks {
    remapJar { archiveClassifier.set("") }

    this.modrinth { dependsOn(remapJar, modrinthSyncBody) }

    processResources {
        doFirst {
            fabricProperty {
                put("owo", libs.versions.owo)
                put("modmenu", libs.versions.modmenu)
                put("minecraft", libs.versions.minecraft)
                put("fabric", libs.versions.fabric)

                val fabricKotlin = libs.fabric.kotlin.get()
                val dep = configurations.modApi
                    .get()
                    .resolvedConfiguration
                    .firstLevelModuleDependencies
                    .single { it.moduleName == fabricKotlin.name && it.moduleGroup == fabricKotlin.group }

                put("fabricKotlin", dep.moduleVersion)
            }
        }
    }
}

System.getenv().getOrDefault("MODRINTH_TOKEN", null)?.let {
    modrinth {
        projectId.set("rjuXQb7H")
        token.set(it)
        versionNumber.set(version.toString())
        versionType.set("alpha")
        uploadFile.set(tasks.remapJar.get())
        loaders.add("fabric")
        syncBodyFrom.set(rootProject.file("README.md").readText())
    }
}
