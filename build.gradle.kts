plugins {
    id 'fabric-loom' version '1.2-SNAPSHOT'
    id 'maven-publish'
    id "org.jetbrains.kotlin.jvm" version "1.8.22"
}

version = project.mod_version
group = project.maven_group

base {
    //noinspection GroovyAccessibility
    archivesName = project.archives_base_name
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    modImplementation "net.fabricmc:fabric-language-kotlin:${project.fabric_kotlin_version}"
}

processResources {
    inputs.property "version", project.version

    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 17
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
    kotlinOptions {
        jvmTarget = 17
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.archivesBaseName}" }
    }
}

// configure the maven publication
publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}