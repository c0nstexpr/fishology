plugins {
    id("fabric.common")
}

tasks.wrapper { gradleVersion = "latest" }

tasks.build { dependsOn(tasks.wrapper) }

dependencies {
    fabricProject(":core")

    include(libs.owo.sentinel)
    implementation(libs.reaktive)
}
