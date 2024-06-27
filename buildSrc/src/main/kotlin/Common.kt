import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import java.util.Optional

inline fun Project.fabricProperty(block: MapProperty<String, String>.() -> Unit) =
    block(extensions.getByType(ModPropertyPluginExtension::class.java).properties)

inline val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

inline fun <R> VersionCatalog.getByName(
    name: String,
    block: VersionCatalog.(String) -> Optional<R>
) = block(name).get()

operator fun VersionCatalog.get(name: String) = getByName(name, VersionCatalog::findLibrary)

val VersionCatalog.bundles get() = VCBundleAccess(this)

class VCBundleAccess internal constructor(versionCatalog: VersionCatalog) :
    VersionCatalog by
    versionCatalog {
        operator fun get(name: String) = getByName(name, VersionCatalog::findBundle)
    }

val VersionCatalog.versions get() = VCVersionsAccess(this)

class VCVersionsAccess internal constructor(versionCatalog: VersionCatalog) :
    VersionCatalog by versionCatalog {
        operator fun get(name: String): String = getByName(name, VersionCatalog::findVersion).run {
            requiredVersion.ifEmpty { strictVersion.ifEmpty { preferredVersion } }
        }
}

const val MOD_JSON = "fabric.mod.json"

inline val Project.sourceSets get() = extensions.getByType<SourceSetContainer>()

inline val Project.srcClient: SourceSet get() = sourceSets["client"]

inline val Project.srcMain: SourceSet get() = sourceSets["main"]
