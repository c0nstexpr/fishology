import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.ModSettings
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import java.util.*

fun DependencyHandler.fabricProject(vararg name: String) = name.forEach {
    add("api", (project(it, "namedElements")))
}

fun Project.fabricProperty(block: MapProperty<String, String>.() -> Unit) =
    block(extensions.getByType(ModPropertyPluginExtension::class.java).properties)

val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun <R> VersionCatalog.getByName(
    name: String,
    block: VersionCatalog.(String) -> Optional<R>
) = block(name).get()

fun VersionCatalog.getLib(name: String) = getByName(name, VersionCatalog::findLibrary)

fun VersionCatalog.getBundle(name: String) = getByName(name, VersionCatalog::findBundle)

fun VersionCatalog.getVersion(name: String): String =
    getByName(name, VersionCatalog::findVersion).run {
        requiredVersion.ifEmpty { strictVersion.ifEmpty { preferredVersion } }
    }
