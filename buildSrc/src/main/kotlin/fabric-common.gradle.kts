import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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

val minecraftLib = libs.getLib("minecraft")
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
    // set mapping without full version catalog support
    // because version catalog has no way to set "v2" classifier
    // https://github.com/gradle/gradle/issues/17169
    mappings("net.fabricmc:yarn:${libs.getVersion("yarn")}:v2")
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

    shadowJar { from(srcClient.output) }

    remapJar {
        dependsOn(shadowJar)
        archiveClassifier.set("remap")
        addNestedDependencies.set(true)
        inputFile.set(shadowJar.get().archiveFile)
    }
}
