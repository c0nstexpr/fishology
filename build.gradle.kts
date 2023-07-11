plugins {
    id("fabric.common")
}

tasks.wrapper {
    gradleVersion = "latest"
}

dependencies {
    fabricProject(":core")

    include(libs.owo.sentinel)
    implementation(libs.reaktive)
}
