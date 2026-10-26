plugins {
    id("azurabedwars.nms-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.21.11-R0.1-SNAPSHOT")
}

// Minecraft 1.20.5 and newer require Java 21
tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}
