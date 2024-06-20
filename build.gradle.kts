plugins { `kotlin-common` }

tasks.wrapper { gradleVersion = "latest" }

spotless {
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
    }

    kotlin {
        ktlint()
    }
}
