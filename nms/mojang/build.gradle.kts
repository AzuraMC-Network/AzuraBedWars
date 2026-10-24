plugins {
    id("azurabedwars.nms-conventions")
}

dependencies {
    // CodeMC lacks paper-server 1.20.5 so the newest 1.21.10 is used
    compileOnly(libs.paper.server)
    compileOnly(libs.paper.api)
}
