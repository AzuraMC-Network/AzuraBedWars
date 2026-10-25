plugins {
    id("azurabedwars.java-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    testImplementation(libs.spigot.api.legacy)
}
