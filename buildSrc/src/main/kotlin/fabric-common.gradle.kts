import juuxel.vineflowerforloom.api.DecompilerBrand
import kotlinx.datetime.Clock.System.now
import net.fabricmc.loom.task.ValidateMixinNameTask

plugins {
    id("kotlin-common")
    id("fabric-loom")
    id("io.github.juuxel.loom-vineflower")
    id("com.modrinth.minotaur")
}

val modVersion: String by project
val modId: String by project
val modName: String by project

println("configuring $modId fabric mod project")

repositories {
    mavenCentral()

    maven("https://maven.fabricmc.net/")
}

base { archivesName.set(modId) }

val libs = versionCatalog

val minecraftLib = libs["minecraft"]
val fabricLib = libs.bundles["fabric"]
val extension = extensions.create<ModPropertyPluginExtension>("modProperties")

extension.properties.run {
    put("id", modId)
    put("version", modVersion)
    put("name", modName)
}

dependencies {
    minecraft(minecraftLib) { isTransitive = false }
    mappings(variantOf(libs["yarn"]) { classifier("v2") }) { isTransitive = false }
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
        //     isIdeConfigGenerated = true
        //     server()
        //     configName = "$modName Server"
        //     source(srcTestModServer)
        // }

        named("client") {
            isIdeConfigGenerated = true
            client()
            configName = "$modName Client"
            source(srcTestModClient)
        }
    }

    runConfigs.forEach { it.runDir = project.relativePath("$rootDir/run") }
}

tasks {
    processResources {
        inputs.property("timestamp", "${now()}")
        filesMatching(modJson) { expand(extension.properties.get()) }
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
