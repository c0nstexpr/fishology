plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
}

val modVersion: String by project
val modGroup: String by project

version = modVersion
group = modGroup

repositories {
    mavenCentral()
    mavenLocal()
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
}

spotless {
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
    }

    kotlin { ktlint() }
}

