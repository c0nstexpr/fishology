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

dependencies {
    clientImplementation(findProject(":${coreProjName}")!!.srcClient.output)
    include(implementation(project(":${coreProjName}:", "namedElements")) { isTransitive = false })

    modImplementation(libs.owo)
    modRuntimeOnly(libs.modmenu)

    listOf(libs.reaktive, libs.kermit).forEach(::shadowApi)}

tasks { this.modrinth { dependsOn(remapJar) } }

System.getenv().getOrDefault("MODRINTH_TOKEN", null)?.let {
    modrinth {
        projectId.set(modId)
        versionNumber.set(modVersion)
        versionType.set("alpha")
        uploadFile.set(tasks.remapJar.get())
    }
}

fabricProperty {
    put("owo", libs.versions.owo)
    put("modmenu", libs.versions.modmenu)
}
