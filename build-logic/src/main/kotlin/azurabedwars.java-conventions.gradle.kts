plugins {
    java
}

val libs = versionCatalogs.named("libs")

java {
    // 26.x server jars are Java 25 bytecode
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    compileOnly(libs.findLibrary("jetbrains-annotations").get())
    compileOnly(libs.findLibrary("lombok").get())
    annotationProcessor(libs.findLibrary("lombok").get())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
    options.isFork = true
    options.forkOptions.memoryMaximumSize = "1536m"
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
