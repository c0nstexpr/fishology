import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project

fun DependencyHandler.fabricProject(vararg name: String) = name.forEach {
    add("api", (project(it, "namedElements")))
}

fun Project.fabricProperty(block: MapProperty<String, String>.() -> Unit) =
    block(extensions.getByType(ModPropertyPluginExtension::class.java).properties)

val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.getLib(name: String) = findLibrary(name).get()

fun VersionCatalog.getVersion(name: String): String = findVersion(name).get().run {
    requiredVersion.ifEmpty { strictVersion.ifEmpty { preferredVersion } }
}
