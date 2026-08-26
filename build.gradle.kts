import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
    id("jacoco")
}

group = "dev.chestwise"
version = providers.gradleProperty("mod.version").get()

dependencies {
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

modSettings {
    clientOptions {
        narrator = false
        musicVolume = 0.0
    }
}
