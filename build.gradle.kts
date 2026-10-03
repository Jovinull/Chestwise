import gg.meza.stonecraft.mod
import java.util.Properties

plugins {
    id("gg.meza.stonecraft")
    id("jacoco")
}

group = "dev.chestwise"
version = providers.gradleProperty("mod.version").get()

val chestwiseLoader = project.name.substringAfterLast('-')
val chestwiseMinecraft = project.name.substringBeforeLast('-')
base {
    archivesName.set("chestwise-$chestwiseLoader-$chestwiseMinecraft")
}

// Pinned beside the other per-version dependencies rather than duplicated here.
val chestwisePins: Properties = rootProject.file("versions/dependencies/$chestwiseMinecraft.properties")
    .inputStream()
    .use { stream -> Properties().apply { load(stream) } }
val chestwiseJei: String = chestwisePins.getProperty("jei_version")
    ?: error("jei_version missing for $chestwiseMinecraft")
val chestwiseRei: String = chestwisePins.getProperty("rei_version")
    ?: error("rei_version missing for $chestwiseMinecraft")

repositories {
    maven("https://maven.blamejared.com")
    maven("https://maven.shedaniel.me")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    // JEI is compileOnly on purpose: Chestwise must load and work with JEI absent,
    // and the plugin class is only touched once JEI itself scans for @JeiPlugin.
    compileOnly("mezz.jei:jei-$chestwiseMinecraft-common-api:$chestwiseJei")
    // Chestwise currently compiles the REI plugin for Forge only on 1.20.1;
    // later Forge nodes omit its API dependency and guard the plugin source out.
    // Mapping differs by artifact: the Forge-family jars are Mojang-mapped, and so
    // is the loader-agnostic one from 26.2 onwards, but the Fabric jars on the
    // older lines are intermediary and have to go through Loom's remapping.
    when (chestwiseLoader) {
        "fabric" -> if (chestwiseMinecraft == "26.2") {
            compileOnly("me.shedaniel:RoughlyEnoughItems-api:$chestwiseRei")
        } else {
            "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-api-fabric:$chestwiseRei")
        }
        // The Forge-family artifacts also carry the @REIPluginClient annotation
        // that those loaders use to discover the plugin.
        "neoforge" -> {
            compileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:$chestwiseRei")
            compileOnly("net.fabricmc:fabric-loader:0.16.9")
        }
        "forge" -> if (chestwiseMinecraft == "1.20.1") {
            compileOnly("me.shedaniel:RoughlyEnoughItems-api-forge:$chestwiseRei")
            compileOnly("net.fabricmc:fabric-loader:0.16.9")
        }
    }
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

val chestwisePackMetadata = when {
    project.name.startsWith("1.20.1-") -> "\"pack_format\": 15"
    project.name.startsWith("1.21.1-") -> "\"pack_format\": 48"
    // 26.2 has distinct resource (88.0) and data (107.1) formats. A mod JAR is both
    // pack types, so its declared supported range must contain both current formats.
    project.name.startsWith("26.2-") -> "\"min_format\": 88,\n    \"max_format\": 107"
    else -> error("No resource-pack format mapped for ${project.name}")
}

tasks.processResources {
    val packMetadata = layout.buildDirectory.file("resources/main/pack.mcmeta")
    outputs.file(packMetadata)
    doLast {
        packMetadata.get().asFile.writeText(
            """{
              "pack": {
                "description": "Chestwise resources",
                $chestwisePackMetadata
              }
            }
            """.trimIndent() + "\n"
        )

        if (project.name.startsWith("26.2-")) {
            val recipe = layout.buildDirectory.file("resources/main/data/chestwise/recipe/storage_terminal.json").get().asFile
            recipe.parentFile.mkdirs()
            recipe.writeText(
                """{
                  "type": "minecraft:crafting_shaped",
                  "pattern": [
                    "IPI",
                    "RCR",
                    "IPI"
                  ],
                  "key": {
                    "I": "minecraft:iron_ingot",
                    "P": "minecraft:glass_pane",
                    "R": "minecraft:redstone",
                    "C": "minecraft:crafting_table"
                  },
                  "result": {
                    "id": "chestwise:storage_terminal",
                    "count": 1
                  }
                }
                """.trimIndent() + "\n"
            )
        }
    }
}

tasks.named<JavaCompile>("compileTestJava").configure {
    tasks.findByName("generatePackMCMetaJson")?.let { generated -> dependsOn(generated) }
}

// Fabric discovers GameTests through a development-only entrypoint. Keep that
// class and entrypoint available to Loom's game-test runs, but do not ship test
// code or an entrypoint referring to it in the release artifact.
if (chestwiseLoader == "fabric") {
    tasks.named<Jar>("jar").configure {
        exclude("dev/chestwise/fabric/ChestwiseGameTests.class")
        exclude("dev/chestwise/fabric/ChestwiseGameTests$*.class")
        filesMatching("fabric.mod.json") {
            filter { line: String ->
                if (line.contains("\"fabric-gametest\"")) null else line
            }
        }
    }
}

modSettings {
    clientOptions {
        narrator = false
        musicVolume = 0.0
    }
}
