# Compatibility policy

Chestwise adapts physical inventories through loader-native contracts rather
than hard-coding individual storage mods.

| Loader family | Integration contract |
| --- | --- |
| Fabric and Quilt | Fabric Transfer API item storage |
| Forge | `IItemHandler` item capability |
| NeoForge 1.20.1 | legacy Forge-compatible item capability |
| NeoForge 1.21.1 | `IItemHandler` item capability |
| NeoForge 26.2 | item `ResourceHandler` capability |

Vanilla `Container` block entities are handled directly. This includes chests,
trapped chests, barrels, and placed shulker boxes. Composite handlers are
deduplicated by the identity exposed by the loader API, and double chests retain
their physical slot ownership without counting one half twice.

An inventory mod should work when its placed block exposes the standard contract
for the active loader and implements simulation consistently. This is an API
compatibility statement, not a claim that every version of Storage Drawers,
Sophisticated Storage, Create, Iron Chests, or another third-party mod has been
certified. Explicit combinations belong in the release checklist only after a
real integration test with the published dependency set.

## Recipe viewers

The terminal's crafting grid accepts a recipe pushed from a recipe viewer. The
grid is filled from the player's inventory first and topped up from the
surrounding containers, so a recipe transfers even when the ingredients are only
in the chests.

| Viewer | Coverage | How |
| --- | --- | --- |
| JEI | compiled on every supported loader and Minecraft version; runtime certification pending viewer smoke tests | `IRecipeTransferHandler` |
| EMI | experimental: JEMI may reuse the JEI handler, but EMI-native recipes require explicit runtime validation | no EMI-specific plugin |
| REI | compiled for Fabric on every line; NeoForge on 1.21.1 and 26.2; Forge on 1.20.1; runtime certification pending viewer smoke tests | `TransferHandler` |

REI publishes no Forge build after 1.20.1, so the REI plugin is compiled out for
Forge on 1.21.1 and 26.2. That is an upstream gap, not a deliberate limitation,
and it is the only place where the nine build variants differ in behaviour.

JEI and REI are compile-only dependencies. None is bundled or required;
Chestwise behaves identically with no recipe viewer installed. EMI is neither
bundled nor declared as a dependency. Its JEMI bridge is an upstream feature and
is not a substitute for testing EMI itself.

The handlers deliberately read their ingredients from the viewer's slot view
rather than from the recipe object, and send the acceptable item ids for each
slot. That keeps them clear of recipe APIs that differ on every Minecraft
generation and lets a tag ingredient be satisfied by whichever member the
player actually owns. The trade-off is deliberate: recipes whose input depends
on NBT/data components, a required stack count, or a non-item ingredient cannot
be represented faithfully and must not be advertised as supported transfer
targets.

Chestwise never opens item-contained inventories recursively, force-loads a
chunk, accesses another dimension, or treats player backpacks as terminal
storage. Inventory protection/claim mods may deny access through their own
hooks; server operators should test their permission stack before deployment.
