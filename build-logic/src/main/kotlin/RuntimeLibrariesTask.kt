import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Writes the runtime library manifest and copies every jar into the bundle
 *
 * Manifest lines and bundle names must match Library in bootstrap
 */
@CacheableTask
abstract class RuntimeLibrariesTask : DefaultTask() {

    /** Paired with [files] by position */
    @get:Input
    abstract val coordinates: ListProperty<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val files: ConfigurableFileCollection

    @get:OutputFile
    abstract val manifest: RegularFileProperty

    @get:OutputDirectory
    abstract val bundle: DirectoryProperty

    @get:Inject
    protected abstract val fileSystem: FileSystemOperations

    @TaskAction
    fun generate() {
        val entries = coordinates.get().zip(files.files.toList())
        manifest.get().asFile.writeText(entries.joinToString("\n", postfix = "\n") { (coordinate, file) ->
            "$coordinate ${sha256(file.readBytes())}"
        })
        fileSystem.delete { delete(bundle) }
        val bundleDir = bundle.get().asFile
        for ((coordinate, file) in entries) {
            val (group, artifact, version) = coordinate.split(":")
            file.copyTo(bundleDir.resolve("$group/$artifact-$version.jarinjar"))
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
}
