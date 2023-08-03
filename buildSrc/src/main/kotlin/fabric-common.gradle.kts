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
val fabricLib = libs.getBundle("fabric")

val extension = extensions.create<ModPropertyPluginExtension>("modProperties")
val srcClient: SourceSet get() = sourceSets["client"]

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

    mods.register(name) { sourceSet(srcClient) }

    runs {
        named("client") {
            client()
            configName = "$modName Client"
            isIdeConfigGenerated = true
            source(srcClient)
        }
    }
}

tasks {
    processResources {
        inputs.property("properties", extension.properties)
//        srcClient.resources.srcDirs.map { it.toPath() }.forEach {
//            from(it.resolve(modJson)) {
//                filesMatching(modJson) {
//                    expand(extension.properties.get())
//                }
//            }
//        }

        filesMatching("**/$modJson") { expand(extension.properties.get()) }

        project.dependencies {
            clientOutputs.each {
                clientImplementation(it)
            }
        }
    }

    withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
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