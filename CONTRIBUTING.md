# Contributing to Chestwise

Bug reports and focused pull requests are welcome. Include the Minecraft
version, loader and loader version, Chestwise JAR filename, relevant logs, and a
minimal reproduction. Never upload a world without making a backup first.

## Development

Use the checked-in Gradle wrapper. Java 17 is required for 1.20.1, Java 21 for
1.21.1, and Java 25 for 26.2; Gradle's toolchain resolver can provision them.

Keep gameplay logic in `src/main/java/dev/chestwise/core` or the shared
Minecraft layer. Loader APIs belong only in their Fabric, Forge, or NeoForge
bridges. Do not add a runtime dependency merely to avoid a small bridge.

Before submitting a change:

```shell
./gradlew test
./gradlew :1.20.1-fabric:runGameTestServer :1.21.1-fabric:runGameTestServer :26.2-fabric:runGameTestServer
```

Changes affecting packaging must build all nine artifacts and pass
`python scripts/audit_artifacts.py`. Compiler warnings are errors.

By contributing, you agree that your contribution is licensed under MIT.
