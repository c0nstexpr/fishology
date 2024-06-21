plugins { `kotlin-common` }

tasks.wrapper { gradleVersion = "latest" }

spotless {
    java { eclipse().configFile("eclipse-perf.xml", "eclipse.importorder") }
}
