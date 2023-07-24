import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.project

fun DependencyHandler.fabricProject(vararg name: String) = name.forEach {
    add("api", (project(it, "namedElements")))
}

fun Project.fabricProperty(block: MapProperty<String, String>.() -> Unit) =
    block(extensions.getByType(ModPropertyPluginExtension::class.java).properties)
