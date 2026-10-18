import java.util.Properties

plugins {
    id("gg.meza.stonecraft")
    id("jacoco")
}

group = providers.gradleProperty("mod.group").get()
version = providers.gradleProperty("mod.version").get()

val chestwiseLoader = project.name.substringAfterLast('-')
val chestwiseMinecraft = project.name.substringBeforeLast('-')
val chestwiseRepository = rootProject.file(
    providers.gradleProperty("chestwise.sharedRoot").get()
)
base {
    archivesName.set("chestwise-$chestwiseLoader-$chestwiseMinecraft")
}

val chestwisePins = Properties().apply {
    chestwiseRepository.resolve("versions/dependencies/$chestwiseMinecraft.properties")
        .inputStream().use(::load)
}
val chestwiseJei = chestwisePins.getProperty("jei_version")
    ?: error("jei_version missing for $chestwiseMinecraft")
val chestwiseRei = chestwisePins.getProperty("rei_version")
    ?: error("rei_version missing for $chestwiseMinecraft")
val chestwiseFabricLoader = chestwisePins.getProperty("loader_version")
    ?: error("loader_version missing for $chestwiseMinecraft")
val chestwiseUsesRei = chestwiseLoader == "fabric" || chestwiseLoader == "neoforge"

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") {
        content { includeGroup("mezz.jei") }
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
            content { includeGroupByRegex("net\\.fabricmc(\\..*)?") }
        }
    }
}

dependencies {
    compileOnly("mezz.jei:jei-$chestwiseMinecraft-common-api:$chestwiseJei")
    when (chestwiseLoader) {
        "fabric" -> compileOnly("me.shedaniel:RoughlyEnoughItems-api:$chestwiseRei")
        "neoforge" -> {
            compileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:$chestwiseRei")
            compileOnly("net.fabricmc:fabric-loader:$chestwiseFabricLoader")
        }
    }
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated/stonecutter/main/java"))
        resources.srcDir(layout.buildDirectory.dir("generated/stonecutter/main/resources"))
    }
    test {
        java.srcDir(rootProject.file("src/test/java"))
        java.srcDir(layout.buildDirectory.dir("generated/stonecutter/test/java"))
        resources.srcDir(rootProject.file("src/test/resources"))
        resources.srcDir(layout.buildDirectory.dir("generated/stonecutter/test/resources"))
    }
}

tasks.named("compileJava") { dependsOn("stonecutterGenerate") }
tasks.named("processResources") { dependsOn("stonecutterGenerate") }
tasks.named("compileTestJava") { dependsOn("stonecutterGenerateTest") }
tasks.named("processTestResources") { dependsOn("stonecutterGenerateTest") }

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

val chestwisePackMetadata = "\"min_format\": 97,\n    \"max_format\": 121"
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

tasks.named<JavaCompile>("compileTestJava").configure {
    tasks.findByName("generatePackMCMetaJson")?.let { dependsOn(it) }
}

if (chestwiseLoader == "fabric") {
    tasks.named<Jar>("jar").configure {
        exclude("dev/chestwise/fabric/ChestwiseGameTests.class")
        exclude("dev/chestwise/fabric/ChestwiseGameTests$*.class")
        filesMatching("fabric.mod.json") {
            filter { line: String -> if (line.contains("\"fabric-gametest\"")) null else line }
        }
    }
}

modSettings {
    clientOptions {
        narrator = false
        musicVolume = 0.0
    }
}
