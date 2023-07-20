import org.gradle.api.provider.MapProperty

interface ModPropertyPluginExtension {
    val properties: MapProperty<String, String>
}
