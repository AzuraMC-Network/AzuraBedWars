plugins {
    id("azurabedwars.nms-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.20.1-R0.1-SNAPSHOT")
}

// Minecraft 1.18 and newer require Java 17
tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}
