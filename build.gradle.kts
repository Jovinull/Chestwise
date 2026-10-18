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
val chestwiseFabricLoader: String = chestwisePins.getProperty("loader_version")
    ?: error("loader_version missing for $chestwiseMinecraft")
val chestwiseUsesRei = chestwiseLoader == "fabric" || chestwiseLoader == "neoforge" ||
    (chestwiseLoader == "forge" && chestwiseMinecraft == "1.20.1")

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") {
        content {
            includeGroup("mezz.jei")
        }
    }
    if (chestwiseUsesRei) {
        maven("https://maven.shedaniel.me") {
            content {
                includeGroupByRegex("me\\.shedaniel(\\..*)?")
                includeGroup("dev.architectury")
            }
        }
    }
    if (chestwiseLoader == "fabric" || chestwiseUsesRei) {
        maven("https://maven.fabricmc.net/") {
            content {
                includeGroupByRegex("net\\.fabricmc(\\..*)?")
            }
        }
    }
}

dependencies {
    // JEI is compileOnly on purpose: Chestwise must load and work with JEI absent,
    // and the plugin class is only touched once JEI itself scans for @JeiPlugin.
    compileOnly("mezz.jei:jei-$chestwiseMinecraft-common-api:$chestwiseJei")
    // Chestwise currently compiles the REI plugin for Forge only on 1.20.1;
    // later Forge nodes omit its API dependency and guard the plugin source out.
    // Mapping differs by artifact: Forge-family jars are Mojang-mapped, while
    // Fabric jars on the supported legacy lines use intermediary mappings.
    when (chestwiseLoader) {
        "fabric" -> "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-api-fabric:$chestwiseRei")
        // The Forge-family artifacts also carry the @REIPluginClient annotation
        // that those loaders use to discover the plugin.
        "neoforge" -> {
            compileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:$chestwiseRei")
            compileOnly("net.fabricmc:fabric-loader:$chestwiseFabricLoader")
        }
        "forge" -> if (chestwiseMinecraft == "1.20.1") {
            compileOnly("me.shedaniel:RoughlyEnoughItems-api-forge:$chestwiseRei")
            compileOnly("net.fabricmc:fabric-loader:$chestwiseFabricLoader")
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
