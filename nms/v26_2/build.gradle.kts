plugins {
    id("azurabedwars.nms-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:26.2-R0.1-SNAPSHOT")
}

// Minecraft 26.1 and newer require Java 25
tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

// Paper changes member access on the same Minecraft version
val paperClasspath = configurations.create("paperClasspath")

dependencies {
    paperClasspath(libs.paper.server)
    paperClasspath(libs.paper.api)
    paperClasspath(libs.datafixerupper)
    paperClasspath(project(":api"))
    paperClasspath(libs.jetbrains.annotations)
    paperClasspath(libs.lombok)
}

val compilePaperJava = tasks.register<JavaCompile>("compilePaperJava") {
    source(sourceSets.main.map { it.java })
    classpath = paperClasspath
    options.annotationProcessorPath = configurations.annotationProcessor.get()
    destinationDirectory = layout.buildDirectory.dir("classes/java/paper")
    javaCompiler = javaToolchains.compilerFor(java.toolchain)
}

tasks.check {
    dependsOn(compilePaperJava)
}
