plugins {
    id("azurabedwars.java-conventions")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    rootProject.project(":nms").subprojects.forEach { implementation(project(it.path)) }
}

base {
    archivesName = "AzuraBedWars"
}

tasks.shadowJar {
    archiveClassifier = ""
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
