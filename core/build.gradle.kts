plugins {
    id("azurabedwars.java-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":bootstrap"))

    compileOnly(libs.spigot.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.packetevents.spigot)

    implementation(libs.commons.lang3)
    implementation(libs.commons.io)
    implementation(libs.lamp.common)
    implementation(libs.lamp.bukkit)
    implementation(libs.xseries)
    implementation(libs.fastboard)
    implementation(libs.mongodb.driver.sync)

    implementation(files("libs/AzuraORM-1.1.2-all.jar"))
    implementation(files("libs/AzuraMobAiController-1.0-SNAPSHOT-all.jar"))
}
