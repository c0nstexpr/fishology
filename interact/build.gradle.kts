plugins {
    `kotlin-common`
    `fabric-common`
}

repositories {
    maven("https://maven.wispforest.io")
}

dependencies {
    fabricProject(":core")

    modImplementation(libs.owo)
}
