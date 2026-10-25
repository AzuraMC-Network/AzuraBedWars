plugins {
    id("azurabedwars.nms-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:26.1-R0.1-SNAPSHOT")
}

// Minecraft 26.1 and newer require Java 25
tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}
