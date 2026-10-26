import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.zip.ZipFile

/**
 * Fails when a jar has classes newer than [javaVersion]
 */
abstract class CheckBytecodeTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val jars: ConfigurableFileCollection

    @get:Input
    abstract val javaVersion: Property<Int>

    @TaskAction
    fun check() {
        val maxMajor = javaVersion.get() + 44
        val offenders = jars.files.mapNotNull { jar ->
            ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    // multi-release overrides and module descriptors are skipped by older JVMs
                    .filter { it.name.endsWith(".class") && !it.name.startsWith("META-INF/versions/") && !it.name.endsWith("module-info.class") }
                    .map { entry -> zip.getInputStream(entry).use { it.readNBytes(8) } }
                    .map { header -> (header[6].toInt() and 0xFF shl 8) or (header[7].toInt() and 0xFF) }
                    .maxOrNull()
                    ?.takeIf { it > maxMajor }
                    ?.let { "${jar.name} needs Java ${it - 44}" }
            }
        }
        if (offenders.isNotEmpty()) {
            throw GradleException("Runtime libraries must run on Java ${javaVersion.get()}:\n" + offenders.joinToString("\n"))
        }
    }
}
