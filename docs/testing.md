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

On Minecraft 1.20.1, the startup message `Loaded 7 recipes` is not a total of
seven recipe IDs. The version's `RecipeManager` groups its recipe map by the
seven recipe types; the GameTest separately asserts that `getRecipeIds()` exposes
more than 1,000 IDs and verifies a real vanilla crafting-table recipe.

The Fabric GameTest entrypoint and `ChestwiseGameTests` class are development
only. Loom keeps them available to these runs, while the production `jar` task
explicitly removes both the class (including any future nested classes) and the
`fabric-gametest` entrypoint from published Fabric artifacts.

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

The production Fabric artifacts were booted on Quilt Loader 0.30.0 on all
three supported Minecraft lines on 2026-10-02. Chestwise was discovered,
initialized, and each dedicated server reached its ready marker. This is a
server-startup compatibility smoke; it does not certify placing the terminal
or opening its client screen under Quilt.

## Packaged loader smoke tests

`scripts/loader_smoke_test.py` installs a clean official Fabric, Forge, or
NeoForge dedicated server, copies the release JAR (and Fabric API where needed),
waits for both Chestwise initialization and Minecraft's ready marker, sends
`stop`, and requires exit code zero. The release workflow runs this against all
nine loader/version combinations before publishing any GitHub release assets.

## Recipe viewers

Recipe-viewer integrations are optional. A successful compile is not runtime
certification.

| Viewer | Combination | Evidence |
| --- | --- | --- |
| JEI 19.44.0.406 | Minecraft 1.21.1, Fabric | Startup and vanilla oak-log-to-oak-planks transfer/craft certified; occupied-grid, insufficient-input, full-capacity, and remainder cases not certified. |
| JEI | Other supported loader/version nodes | Compile-tested only. |
| EMI 1.1.23+1.21.1+fabric + JEI 19.44.0.406 | Minecraft 1.21.1, Fabric | Startup, oak-log-to-planks transfer/craft, occupied-grid return, and clean insufficient-diamond-input failure tested through the JEMI plugin. No native Chestwise EMI plugin. Remainder/full-capacity cases not tested. |
| REI | Fabric 1.20.1/1.21.1/26.2; Forge 1.20.1; NeoForge 1.20.1/1.21.1/26.2 | Compile-tested only; no runtime transfer certification. |
| REI | Forge 1.21.1 and 26.2 | No Chestwise REI handler is built for these nodes. |

The JEI runtime transfer test confirms plugin discovery, one ingredient
withdrawn from a physical chest into the terminal grid, and the resulting
four-plank craft. Through EMI's JEMI plugin, the same Chestwise transfer handler
was observed returning an existing crafting-table input, filling the oak-plank
recipe, and crafting four planks; a diamond-pickaxe transfer with no diamonds
left the grid and storage unchanged. Remainders, completely full capacity, and
all JEI transfer edge cases remain untested. During EMI reload the log contained
duplicate `jei:/...` recipe-ID errors, though the tested transfers completed and
no Chestwise linkage exception was observed. REI has compile coverage only in
the listed nodes.

## Manual client observations

On Minecraft 1.21.1 Fabric, the terminal screen was opened in a real client
with EMI and JEI installed. The visible search/magnifier, aggregate items,
crafting inputs/result, player inventory, and action labels were aligned at
GUI scale Auto and 3; ESC closed the terminal. GUI scale 2 was selected in
Video Settings, but the terminal screen was not reopened at that scale. The
inventory key opened the creative inventory after the terminal had been
closed. This does not certify all GUI scaling, search-key, or tooltip cases.

The full-stack carried-item deposit path was observed in an earlier manual
run. Partial-capacity, full-storage, right-click, double-click, drag/quick-craft,
and close-with-carried-stack cases remain untested in a real client. A Fabric
GameTest now drives `PICKUP_ALL` with a same-variant cursor stack and verifies
that it cannot withdraw more items; the existing GameTest also verifies that
`QUICK_CRAFT` leaves the carried stack unchanged. These server-side click
regressions do not replace the uncompleted manual capacity/drag checks.

Quilt 0.30.0 server startup passed for all three Fabric artifacts (see the
dedicated-server smoke evidence above). No Quilt client was launched, so client
screen/render compatibility remains unverified for 1.20.1, 1.21.1, and 26.2.

## Release evidence

A passing dedicated-server boot establishes loader, entrypoint, registry,
metadata, recipe, loot table, and server-side classloading compatibility. It does
not replace client UI testing, multiplayer interaction testing, long-running
world testing, or explicit third-party-mod integration tests; those are tracked
separately in the release checklist.
