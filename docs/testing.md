# Testing Chestwise

Chestwise treats loader compatibility as a runtime claim, not only a compilation
claim. Every supported build runs the shared JUnit suite, and packaged artifacts
are booted on their target dedicated server before release.

## Unit tests

Run every normal Stonecutter node:

```shell
./gradlew test
```

The suite covers exact item identity, aggregation, search syntax, sorting,
incremental invalidation, withdrawal planning, deposit priority, safe matching
deposits, configuration validation, compact count formatting, and the recipe
transfer wire format.

## Fabric game tests

Fabric nodes include server game tests that place real containers, discover and
index them, withdraw and deposit through the terminal backend, invalidate removed
containers, resolve a crafting recipe through Minecraft's recipe manager, keep a
shared crafting grid and result coherent across simultaneously open menus,
return that grid to storage when the terminal is broken, and fill a recipe from
the inventory before storage without overwriting an unreturned grid stack.

Game tests all run in one world at the same time, and a terminal indexes every
container within its scan radius. Each test therefore has to use an item no other
test touches, or terminals index one another's chests and the suite turns flaky.

```shell
./gradlew :1.20.1-fabric:runGameTestServer
./gradlew :1.21.1-fabric:runGameTestServer
./gradlew :26.2-fabric:runGameTestServer
```

## Quilt compatibility

The Quilt check installs an official Quilt dedicated server, copies the already
packaged Fabric artifact into `mods`, adds the matching Fabric API, waits for
Chestwise initialization and the server ready marker, sends `stop`, and requires
a clean exit. It deliberately does not use development classes or QFAPI.

Example:

```shell
python scripts/quilt_smoke_test.py \
  --minecraft 1.21.1 \
  --fabric-api 0.116.15+1.21.1 \
  --artifact versions/1.21.1-fabric/build/libs/chestwise-fabric-1.21.1-0.1.0.jar
```

The official compatibility matrix uses Quilt Loader 0.30.0 for Minecraft
1.20.1, 1.21.1, and 26.2. QSL and QFAPI are not runtime dependencies.

## Packaged loader smoke tests

`scripts/loader_smoke_test.py` installs a clean official Fabric, Forge, or
NeoForge dedicated server, copies the release JAR (and Fabric API where needed),
waits for both Chestwise initialization and Minecraft's ready marker, sends
`stop`, and requires exit code zero. The release workflow runs this against all
nine loader/version combinations before publishing any GitHub release assets.

## Recipe viewers

Recipe transfer is optional and compile-only, so the first thing to verify is
that the mod behaves identically with no viewer installed. Then install one
viewer at a time and use its transfer button on a recipe the player cannot fully
supply from their own inventory; the missing ingredients must come out of the
surrounding containers.

| Viewer | Version used for the 1.20.1 matrix |
| --- | --- |
| JEI | 15.49.0.194 |
| REI | 12.1.785 |
| EMI | 1.0.9+1.20.1 |

EMI has no Chestwise-specific code. Its JEMI bridge can invoke the JEI handler,
including for an EMI-native recipe, because the handler intentionally does not
cast or inspect the recipe object. This remains a runtime test requirement, not
a certification claim. REI ships no Forge build after 1.20.1, so there is
nothing to test for Forge on 1.21.1 and 26.2.

## Release evidence

A passing dedicated-server boot establishes loader, entrypoint, registry,
metadata, recipe, loot table, and server-side classloading compatibility. It does
not replace client UI testing, multiplayer interaction testing, long-running
world testing, or explicit third-party-mod integration tests; those are tracked
separately in the release checklist.
