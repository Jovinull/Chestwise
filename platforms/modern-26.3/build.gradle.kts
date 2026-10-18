import org.gradle.api.tasks.Sync

val chestwiseRepository = layout.projectDirectory.dir("../..").asFile
val localVersionPins = layout.projectDirectory.file("versions/dependencies/26.3.properties").asFile
val sharedVersionPins = chestwiseRepository.resolve("versions/dependencies/26.3.properties")

val verifyChestwiseVersionPins by tasks.registering {
    inputs.files(localVersionPins, sharedVersionPins)
    doLast {
        if (!localVersionPins.readBytes().contentEquals(sharedVersionPins.readBytes())) {
            throw GradleException("The isolated 26.3 pin file differs from the canonical versions/dependencies/26.3.properties")
        }
    }
}

val syncChestwiseSources by tasks.registering(Sync::class) {
    from(chestwiseRepository.resolve("src"))
    into(layout.projectDirectory.dir("src"))
}

subprojects {
    tasks.configureEach {
        if (name.startsWith("stonecutterPrepare") || name.startsWith("stonecutterGenerate")) {
            dependsOn(syncChestwiseSources)
            dependsOn(verifyChestwiseVersionPins)
        }
    }
}
