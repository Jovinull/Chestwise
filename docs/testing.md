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
the inventory before storage without overwriting an unreturned grid stack. They
also verify an insufficient recipe transfer does not consume a partial set of
ingredients or change the pre-existing grid, and that a complete assignment is
found when ingredient alternatives overlap.

Game tests all run in one world at the same time, and a terminal indexes every
container within its scan radius. Each test therefore has to use an item no other
test touches, or terminals index one another's chests and the suite turns flaky.

Restock planner unit tests cover satisfied and partial targets, source and
destination limits, multi-stack quantities, protected slots, exact item identity,
duplicate/update/remove behavior, independent targets, and item conservation.
Fabric GameTests exercise the server menu against real nearby chests, including
partial availability, capacity, protected slots, damaged-item variants,
independent and concurrent demands, target editing, and distance validation.

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

The dedicated-server compatibility matrix uses Quilt Loader 0.30.0 for Minecraft
1.20.1, 1.21.1, and 26.2. Client smokes use Quilt Loader 0.30.1. QSL and QFAPI
are not runtime dependencies.

On all three supported lines, the production Fabric artifact was discovered by
Quilt and its dedicated server reached the ready marker. In separate Quilt
client smokes, a world loaded, the Storage Terminal could be obtained and placed,
its block rendered, and the screen opened, accepted search text, and closed with
Escape. These are startup and basic interaction checks, not exhaustive
cross-mod compatibility tests.

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
| JEI 19.44.0.406 | Minecraft 1.21.1, Fabric | Startup; vanilla transfer/craft; occupied-grid return; full-storage refusal; and insufficient-input refusal certified. Cake remainder behavior is covered by the RecipeManager GameTest, not independently through JEI. |
| JEI | Other supported loader/version nodes | Compile-tested only. |
| EMI 1.1.23+1.21.1+fabric + JEI 19.44.0.406 | Minecraft 1.21.1, Fabric | Startup, oak-log-to-planks transfer/craft, occupied-grid return, and insufficient-input refusal tested through JEMI. No native Chestwise EMI plugin. |
| REI | Fabric 1.20.1/1.21.1/26.2; Forge 1.20.1; NeoForge 1.20.1/1.21.1/26.2 | Compile-tested only; no runtime transfer certification. |
| REI | Forge 1.21.1 and 26.2 | No Chestwise REI handler is built for these nodes. |

The JEI runtime checks confirm plugin discovery, a physical-chest ingredient
withdrawn into the terminal grid, and the resulting four-plank craft. An occupied
grid was returned before a different transfer; when the chest was full and the
old grid item could not be returned, the old grid and inventory remained intact.
After the insufficiency fix, a diamond-pickaxe transfer with only one diamond
available left that diamond in storage and the grid empty. EMI used the JEMI/JEI
bridge for the same handler; startup, transfer/craft, occupied-grid return, and
insufficient-input refusal passed. EMI logged duplicate `jei:/...` IDs during
recipe baking after JEMI's collection phase; this was an upstream bridge log,
with no Chestwise exception or failure in the tested transfers. REI remains
compile-tested only in the listed nodes. Cake-bucket remainders are exercised by
the vanilla RecipeManager GameTest and were not separately attributed to JEI.

## Manual client observations

Manual client checks on Minecraft 1.21.1 Fabric covered GUI scales Auto, 2, and
3; search typing/filtering, backspace and clear; ESC; tooltips for storage,
crafting inputs and result; visible counters/labels; and clicks on storage,
crafting, result, inventory, hotbar, and sort controls. The inventory key closes
the screen after search focus leaves the text field. Typing `e` while search is
focused no longer closes the screen; Escape still closes it.

Carried-stack deposit was checked with a full stack, a destination with only
partial capacity, a full destination, right-click, and closing with a remainder
on the cursor. Observed counts were conserved: a 64-stack with 10 spaces left
inserted 10 and preserved 54; a full destination left the carried stack intact;
right-click deposited one; closing returned the remainder to the player. A
double-click on an aggregate withdrew only once. `QUICK_CRAFT` and `PICKUP_ALL`
are additionally covered by server GameTests that verify their click stages do
not act as an unintended deposit or withdrawal.

Restock was manually exercised on Minecraft 1.21.1 Fabric. With a target of 64,
17 torches in the player inventory, and 20 in a nearby chest, it moved only the
available 20. The target could be edited, removed, and added again, and remained
saved after Save and Quit followed by reopening the world. With the player
inventory full, a target of 100 and 64 torches in storage moved only 27 into the
remaining space of the existing stack; the player reached 64 and the chest kept
37. Repeating Restock with no remaining player capacity left the chest unchanged.

Quilt client smoke passed on 1.20.1, 1.21.1, and 26.2 with the production Fabric
artifact: Chestwise discovery, world load, terminal placement/rendering, screen
open, search input, and Escape close. Dedicated-server Quilt checks are listed
separately above.

## Release evidence

A passing dedicated-server boot establishes loader, entrypoint, registry,
metadata, recipe, loot table, and server-side classloading compatibility. It does
not replace client UI testing, multiplayer interaction testing, long-running
world testing, or explicit third-party-mod integration tests; those are tracked
separately in the release checklist.
