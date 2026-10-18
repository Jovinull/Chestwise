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
    id("gg.meza.stonecraft") version "1.14.5"
    id("dev.kikugie.stonecutter") version "0.9.8"
}

rootProject.name = "chestwise-26.3-build"

stonecutter {
    centralScript = "platforms/modern-26.3/version.gradle.kts"
    kotlinController = true
    shared {
        version("26.3-fabric", "26.3")
        version("26.3-neoforge", "26.3")
        vcsVersion = "26.3-fabric"
    }
    create(rootProject)
}

project(":26.3-fabric").projectDir = file("../../versions/26.3-fabric")
project(":26.3-neoforge").projectDir = file("../../versions/26.3-neoforge")

include("forge-26.3")
project(":forge-26.3").projectDir = file("../forge-26.3")
