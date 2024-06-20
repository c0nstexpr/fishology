import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode

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
        sourceCompatibility = libs.versions["jvm"]
        targetCompatibility = sourceCompatibility
        options.encoding = Charsets.UTF_8.name()
    }

    compileKotlin {
        compilerOptions {
            jvmTargetValidationMode.set(JvmTargetValidationMode.ERROR)
            jvmTarget.set(JvmTarget.fromTarget(compileJava.get().targetCompatibility))
            allWarningsAsErrors = true
            languageVersion.set(KotlinVersion.fromVersion(libs.versions["kotlin"]))
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

    kotlin {
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_chain_method_rule_force_multiline_when_chain_operator_count_greater_or_equal_than" to 1,
                "ktlint_standard_if-else-bracing" to "disabled",
                "ktlint_standard_multiline-if-else" to "disabled",
                "ktlint_standard_multiline-loop" to "disabled",
                "ktlint_standard_multiline-expression-wrapping" to "disabled"
            )
        )
    }
}
