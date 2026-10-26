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
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Minecraft 1.8 to 1.16 servers still run on Java 8
    options.release = 8
    options.compilerArgs.add("-Xlint:-options")
    options.isFork = true
    options.forkOptions.memoryMaximumSize = "1536m"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
