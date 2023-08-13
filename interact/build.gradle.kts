plugins {
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
}

val coreProjName = "core"
val modVersion: String by project
val modId: String by project
val depends = listOf(libs.reaktive, libs.kermit)

dependencies {
    clientImplementation(findProject(":${coreProjName}")!!.srcClient.output)

    val coreProj = project(":${coreProjName}:", "namedElements")

    include(implementation(coreProj)!!)

    depends.forEach { fabricLibrary(it, tasks) }

    modImplementation(libs.owo)
    modRuntimeOnly(libs.modmenu)
}

tasks { this.modrinth { dependsOn(remapJar) } }

System.getenv().getOrDefault("MODRINTH_TOKEN", null)?.let {
    modrinth {
        projectId.set(modId)
        versionNumber.set(modVersion)
        versionType.set("alpha")
        uploadFile.set(tasks.remapJar.get())
    }
}
