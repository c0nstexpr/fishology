plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.spotless)
}


val modVersion: String by project
val modId: String by project
val modName: String by project

base.archivesName.set(modId)

dependencies {
    include(libs.owo.sentinel)
    implementation(libs.reaktive)
}
