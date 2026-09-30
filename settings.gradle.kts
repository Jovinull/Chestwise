pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("gg.meza.stonecraft") version "1.14.2"
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun minecraft(version: String, vararg loaders: String) {
            loaders.forEach { loader -> version("$version-$loader", version) }
        }

        minecraft("1.20.1", "fabric", "forge")
        minecraft("1.21.1", "fabric", "forge", "neoforge")
        minecraft("26.2", "fabric", "neoforge")

        vcsVersion = "1.20.1-fabric"
    }
    create(rootProject)
}

include("legacy-neoforge-1.20.1")
project(":legacy-neoforge-1.20.1").projectDir = file("platforms/legacy-neoforge-1.20.1")
include("forge-26.2")
project(":forge-26.2").projectDir = file("platforms/forge-26.2")

rootProject.name = "chestwise"
