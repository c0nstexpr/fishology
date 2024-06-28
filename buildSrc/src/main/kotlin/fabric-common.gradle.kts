import kotlinx.datetime.Clock.System.now
import net.fabricmc.loom.task.ValidateMixinNameTask

plugins {
    id("kotlin-common")
    id("fabric-loom")
}

val modVersion: String by project
val modId: String by project
val modName: String by project

println("configuring $modId fabric mod project")

repositories {
    maven("https://maven.fabricmc.net/")
}

base { archivesName.set(modId) }

val libs = versionCatalog
val extension = extensions.create<ModPropertyPluginExtension>("modProperties")

extension.properties.run {
    put("id", modId)
    put("version", modVersion)
    put("name", modName)
}

dependencies {
    minecraft(libs["minecraft"]) { isTransitive = false }
    mappings(variantOf(libs["yarn"]) { classifier("v2") }) { isTransitive = false }
    listOf(libs.bundles["fabric"]).forEach(::modApi)
}

loom {
    splitEnvironmentSourceSets()

    mods.create(name) {
        sourceSet(srcMain)
        sourceSet(srcClient)
    }

    runs {
        named("client") {
            isIdeConfigGenerated = true
            client()
            configName = "$modName Client"
        }
    }

    runConfigs.forEach { it.runDir = project.relativePath("$rootDir/run") }
}

tasks {
    processResources {
        inputs.property("timestamp", "${now()}")
        filesMatching(MOD_JSON) { expand(extension.properties.get()) }
    }

    val validateMixinName = register<ValidateMixinNameTask>("validateMixinName") {
        source(srcMain.output)
        source(srcClient.output)
    }

    build { dependsOn(validateMixinName) }

    remapJar {
        from(srcClient.output)
        archiveClassifier.set("remap")
        addNestedDependencies.set(true)
    }
}
