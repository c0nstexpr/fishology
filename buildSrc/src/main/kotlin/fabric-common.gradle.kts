import juuxel.vineflowerforloom.api.DecompilerBrand
import kotlinx.datetime.Clock.System.now
import net.fabricmc.loom.task.ValidateMixinNameTask

plugins {
    id("kotlin-common")
    id("fabric-loom")
    id("com.modrinth.minotaur")
    id("io.github.juuxel.loom-vineflower")
    id("com.github.johnrengelman.shadow")
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

    runConfigs.forEach { it.runDir = project.relativePath("$rootDir/run") }
}

fun Configuration.exclusion() {
//    listOf(
//        "kotlin-stdlib",
//        "kotlin-stdlib-jdk8",
//        "kotlin-stdlib-jdk7",
//        "kotlin-reflect"
//    ).forEach { exclude("org.jetbrains.kotlin", it) }
//
//    listOf(
//        "kotlinx-coroutines-core",
//        "kotlinx-coroutines-core-jvm",
//        "kotlinx-coroutines-jdk8",
//        "kotlinx-serialization-core-jvm",
//        "kotlinx-serialization-json-jvm",
//        "kotlinx-serialization-cbor-jvm",
//        "atomicfu-jvm",
//        "kotlinx-datetime-jvm"
//    ).forEach { exclude("org.jetbrains.kotlinx", it) }

    exclude("com.mojang", "minecraft")
}

val shadowApi: Configuration by configurations.creating { exclusion() }
val shadowImpl: Configuration by configurations.creating { exclusion() }
val shadowInclude: Configuration by configurations.creating { exclusion() }

configurations {
    api { extendsFrom(shadowApi) }
    implementation { extendsFrom(shadowImpl) }
    include { extendsFrom(shadowInclude) }
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

    shadowJar {
        configurations = listOf(shadowApi, shadowImpl, shadowInclude)
        dependencies { exclude("META-INF/**") }
        // minimize()
    }

    remapJar {
        archiveBaseName.set(modId)
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    this.modrinth { dependsOn(remapJar) }
}

System.getenv().getOrDefault("MODRINTH_TOKEN", null)?.let {
    modrinth {
        projectId.set(modId)
        versionNumber.set(modVersion)
        versionType.set("alpha")
        uploadFile.set(tasks.remapJar.get())
    }
}
