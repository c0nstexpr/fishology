import juuxel.vineflowerforloom.api.DecompilerBrand
import kotlinx.datetime.Clock.System.now

plugins {
    id("fabric-loom")
    id("com.modrinth.minotaur")
    id("io.github.juuxel.loom-vineflower")
}

val modVersion: String by project
val modId: String by project
val modName: String by project

println("configuring $modId fabric mod project")

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://maven.fabricmc.net/")
}

val libs = versionCatalog

val minecraftLib = libs.getLib("minecraft")
val yarnMappings = libs.getLib("yarn.mappings")
val fabricLoaderLib = libs.getLib("fabric.loader")
val fabricApiLib = libs.getLib("fabric.api")

val extension = extensions.create<ModPropertyPluginExtension>("modProperties")
extension.properties.run {
    put("id", modId)
    put("version", modVersion)
    put("name", modName)
    put("minecraft", minecraftLib.get().version!!)
    put("fabricApi", fabricApiLib.get().version!!)
    put("fabricLoader", fabricLoaderLib.get().version!!)
}

dependencies {
    minecraft(minecraftLib)
    mappings(yarnMappings)
    modImplementation(fabricLoaderLib)
    modImplementation(fabricApiLib)
}

vineflower.brand.set(DecompilerBrand.VINEFLOWER)

loom {
    runs {
        named("client") {
            client()
            configName = "$modName Client"
            isIdeConfigGenerated = true
        }
    }
}

tasks {
    processResources {
        inputs.property("buildTimestamp", now().epochSeconds)

        filesMatching("fabric.mod.json") { expand(extension.properties.get()) }
    }

    System.getenv().getOrDefault("MODRINTH_TOKEN", null)?.let {
        modrinth {
            token.set(it)
            projectId.set(modId)
            versionNumber.set(modVersion)
            versionType.set("alpha")
            uploadFile.set(tasks.remapJar)
        }
    }
}