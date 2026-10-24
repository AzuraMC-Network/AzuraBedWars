plugins {
    id("azurabedwars.java-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    testImplementation(libs.spigot.api.legacy)
}

// loads on Java 8 servers to report the Java requirement
tasks.compileJava {
    options.release = 8
    options.compilerArgs.add("-Xlint:-options")
}
