import kotlinx.datetime.Clock.System.now

plugins {
    base
    id("org.jetbrains.kotlin.jvm")
    id("fabric-loom")
    id("com.modrinth.minotaur")
    id("com.diffplug.spotless")
    id("io.github.juuxel.loom-vineflower")
}

val modVersion: String by project
val modId: String by project
val modName: String by project

base { archivesName.set(modId) }
version = modVersion
group = "org.c0nstexpr"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://maven.fabricmc.net/")
}

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun getLib(name: String) = libs.findLibrary(name).get()

fun getVersion(name: String) = libs.findVersion(name).get().run {
    requiredVersion.ifEmpty { strictVersion.ifEmpty { preferredVersion } }
}

val minecraftLib = getLib("minecraft")
val yarnMappings = getLib("yarn.mappings")
val fabricLoaderLib = getLib("fabric.loader")
val fabricApiLib = getLib("fabric.api")

val extension =
    extensions.create<ModPropertyPluginExtension>("modProperties")
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

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        sourceCompatibility = getVersion("jvm")
        targetCompatibility = sourceCompatibility
    }

    compileKotlin {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = compileJava.get().targetCompatibility
        }
    }

    processResources {
        inputs.property("buildTimestamp", now().epochSeconds)

        filesMatching("fabric.mod.json") { expand(extension.properties.get()) }
    }

    jar {
        from("LICENSE")
    }

    java {
        withSourcesJar()
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

spotless {
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
    }

    kotlin { ktlint() }
}
