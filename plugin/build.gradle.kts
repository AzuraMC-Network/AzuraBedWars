import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import javax.inject.Inject

plugins {
    id("azurabedwars.java-conventions")
}

dependencies {
    implementation(project(":bootstrap"))
    implementation(project(":api"))
    implementation(project(":core"))
    rootProject.project(":nms").subprojects.forEach { implementation(project(it.path)) }
}

base {
    archivesName = "AzuraBedWars"
}

// nms modules for newer Minecraft target newer Java
java {
    disableAutoTargetJvm()
}

tasks.jar {
    enabled = false
}

interface ArchiveServices {
    @get:Inject
    val archives: ArchiveOperations
}

val archives = objects.newInstance<ArchiveServices>().archives

val runtimeArtifacts = configurations.runtimeClasspath.flatMap { it.incoming.artifacts.resolvedArtifacts }

fun projectJars(paths: Set<String>, outer: Boolean) = runtimeArtifacts.map { artifacts ->
    artifacts.filter {
        val id = it.id.componentIdentifier
        id is ProjectComponentIdentifier && (id.projectPath in paths) == outer
    }.map { it.file }
}

// loaded by Bukkit so other plugins share the API classes and Paper remaps the NMS classes
val outerProjects = setOf(":bootstrap", ":api") + rootProject.project(":nms").subprojects.map { it.path }
val outerProjectJars = projectJars(outerProjects, true)
val innerProjectJars = projectJars(outerProjects, false)

val libraryArtifacts = runtimeArtifacts.map { artifacts ->
    artifacts.filter { it.id.componentIdentifier is ModuleComponentIdentifier }
}

val coreJar = tasks.register<Jar>("coreJar") {
    archiveClassifier = "core"
    destinationDirectory = layout.buildDirectory.dir("intermediates")
    val archives = archives
    from(innerProjectJars.map { jars -> jars.map { archives.zipTree(it) } }) {
        exclude("META-INF/MANIFEST.MF")
    }
}

val runtimeLibraries = tasks.register<RuntimeLibrariesTask>("runtimeLibraries") {
    coordinates.add("${project.group}:azurabedwars-core:${project.version}")
    coordinates.addAll(libraryArtifacts.map { artifacts ->
        artifacts.map {
            val id = it.id.componentIdentifier as ModuleComponentIdentifier
            "${id.group}:${id.module}:${id.version}"
        }
    })
    files.from(coreJar)
    files.from(libraryArtifacts.map { artifacts -> artifacts.map { it.file } })
    manifest = layout.buildDirectory.file("runtime-libraries/libraries.txt")
    bundle = layout.buildDirectory.dir("runtime-libraries/bundle")
}

/** Paths must match LibraryResolver in bootstrap */
fun Jar.pluginContents(archives: ArchiveOperations) {
    from(outerProjectJars.map { jars -> jars.map { archives.zipTree(it) } }) {
        exclude("META-INF/MANIFEST.MF")
    }
    from(runtimeLibraries.flatMap { it.manifest }) {
        into("META-INF/azurabedwars")
    }
}

val pluginJar = tasks.register<Jar>("pluginJar") {
    pluginContents(archives)
    // the core jar is not in public repositories
    from(runtimeLibraries.flatMap { it.bundle }) {
        include("cc.azuramc/**")
        into("META-INF/azurabedwars/libraries")
    }
}

val offlineJar = tasks.register<Jar>("offlineJar") {
    archiveClassifier = "offline"
    pluginContents(archives)
    from(runtimeLibraries.flatMap { it.bundle }) {
        into("META-INF/azurabedwars/libraries")
    }
}

tasks.assemble {
    dependsOn(pluginJar, offlineJar)
}
