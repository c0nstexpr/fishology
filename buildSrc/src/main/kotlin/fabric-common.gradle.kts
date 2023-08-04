import gradle.kotlin.dsl.accessors._cb396132f851efe91a37ba0fe167d944.sourceSets
import juuxel.vineflowerforloom.api.DecompilerBrand

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
val fabricLib = libs.getBundle("fabric")
val extension = extensions.create<ModPropertyPluginExtension>("modProperties")

extension.properties.run {
    put("id", modId)
    put("version", modVersion)
    put("name", modName)
    put("minecraft", minecraftLib.get().version!!)
    put("fabric", libs.getVersion("fabric"))
}

dependencies {
    minecraft(minecraftLib)
    mappings(yarnMappings)
    modImplementation(fabricLib)
}

vineflower.brand.set(DecompilerBrand.VINEFLOWER)

loom {
    splitEnvironmentSourceSets()

    mods.register(name) {
        sourceSet(srcMain)
        sourceSet(srcClient)
    }
}

sourceSets {
    register("testModClient") {
        compileClasspath += srcMain.compileClasspath + srcClient.compileClasspath
        runtimeClasspath += srcMain.runtimeClasspath + srcClient.runtimeClasspath
    }
}

loom {
    runs {
        // named("server") {
        //     server()
        //     configName = "$modName Server"
        //     isIdeConfigGenerated = true
        //     source(srcTestModServer)
        // }

        named("client") {
            client()
            configName = "$modName Client"
            isIdeConfigGenerated = true

            source(srcTestModClient)
        }
    }
}

tasks {
    processResources {
        inputs.property("properties", extension.properties)
        filesMatching(modJson) { expand(extension.properties.get()) }
    }
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