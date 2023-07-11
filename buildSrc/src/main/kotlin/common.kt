import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

fun DependencyHandler.fabricProject(name: String) = add("api", (project(name, "namedElements")))
