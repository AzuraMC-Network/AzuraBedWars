plugins {
    id("azurabedwars.java-conventions")
}

dependencies {
    compileOnly(project(":api"))
    // the oldest API, so the sample runs on every supported server
    compileOnly(libs.spigot.api.legacy)
}

base {
    archivesName = "AzuraBedWars-Grapple"
}
