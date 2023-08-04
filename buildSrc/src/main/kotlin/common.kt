import gradle.kotlin.dsl.accessors._cb396132f851efe91a37ba0fe167d944.sourceSets
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
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

const val modJson = "fabric.mod.json"

val Project.srcClient: SourceSet get() = sourceSets["client"]

val Project.srcMain: SourceSet get() = sourceSets["main"]

val Project.srcTestModServer: SourceSet get() = srcMain

val Project.srcTestModClient: SourceSet get() = sourceSets["testModClient"]
