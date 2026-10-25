plugins {
    id("azurabedwars.nms-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT")
}

// Minecraft 1.17 and newer require Java 16
tasks.withType<JavaCompile>().configureEach {
    options.release = 16
}
