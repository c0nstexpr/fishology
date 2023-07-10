plugins {
    id("fabric.common")
}

val modId: String by project

base.archivesName.set(modId)

dependencies {
    include(libs.owo.sentinel)
    implementation(libs.reaktive)
}
