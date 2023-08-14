import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import java.util.*

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

class VCBundleAccess internal constructor(versionCatalog: VersionCatalog) : VersionCatalog by
versionCatalog {
    operator fun get(name: String) = getByName(name, VersionCatalog::findBundle)
}

val VersionCatalog.versions get() = VCVersionsAccess(this)

class VCVersionsAccess internal constructor(versionCatalog: VersionCatalog) :
    VersionCatalog by versionCatalog {
    operator fun get(name: String) = getByName(name, VersionCatalog::findVersion).run {
        requiredVersion.ifEmpty { strictVersion.ifEmpty { preferredVersion } }
    }
}

const val modJson = "fabric.mod.json"

inline val Project.sourceSets get() = extensions.getByType<SourceSetContainer>()

inline val Project.srcClient: SourceSet get() = sourceSets["client"]

inline val Project.srcMain: SourceSet get() = sourceSets["main"]

inline val Project.srcTestModServer: SourceSet get() = srcMain

inline val Project.srcTestModClient: SourceSet get() = sourceSets["testModClient"]
