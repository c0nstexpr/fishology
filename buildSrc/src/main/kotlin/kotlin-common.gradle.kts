plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
    id("com.github.johnrengelman.shadow")
}

val modVersion: String by project
val modGroup: String by project

version = modVersion
group = modGroup

repositories {
    mavenCentral()
    mavenLocal()
}

val shadowApi by configurations.creating
val shadowImpl by configurations.creating

configurations {
    api { extendsFrom(shadowApi) }
    implementation { extendsFrom(shadowImpl) }
}

val libs = versionCatalog

tasks {
    compileJava {
        sourceCompatibility = libs.getVersion("jvm")
        targetCompatibility = sourceCompatibility
        options.encoding = Charsets.UTF_8.name()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = compileJava.get().targetCompatibility
            allWarningsAsErrors = true
            languageVersion = libs.getVersion("kotlin")
            apiVersion = languageVersion
        }
    }

    jar { from("LICENSE") }

    java { withSourcesJar() }

    shadowJar {
        from("LICENSE")
        configurations = listOf(shadowApi, shadowImpl)
        archiveClassifier.set("shadow")
        mergeServiceFiles()
    }
}

spotless {
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
    }

    kotlin { ktlint() }
}

