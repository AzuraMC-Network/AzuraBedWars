rootProject.name = "AzuraBedWars"

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            content {
                includeGroup("org.spigotmc")
                includeGroup("org.bukkit")
                includeGroup("net.md-5")
            }
        }
        maven("https://repo.codemc.io/repository/nms/") {
            content {
                includeGroup("org.spigotmc")
                includeGroup("io.papermc.paper")
            }
        }
        maven("https://repo.codemc.io/repository/maven-releases/") {
            content {
                includeGroup("com.github.retrooper")
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github\\..*")
            }
        }
        maven("https://repo.extendedclip.com/releases/") {
            content {
                includeGroup("me.clip")
            }
        }
    }
}

include("api", "bootstrap", "core", "plugin")

rootDir.resolve("nms").listFiles()!!
    .filter { it.resolve("build.gradle.kts").isFile }
    .sortedBy { it.name }
    .forEach { include(":nms:${it.name}") }
