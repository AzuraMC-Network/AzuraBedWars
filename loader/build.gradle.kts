plugins {
    id("azurabedwars.java-conventions")
}

version = "1.0.0"

base {
    archivesName = "AzuraBedWarsLoader"
}

dependencies {
    compileOnly(libs.spigot.api)
}
