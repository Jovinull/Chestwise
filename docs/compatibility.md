# Compatibility policy

Chestwise adapts physical inventories through loader-native contracts rather
than hard-coding individual storage mods.

| Loader family | Integration contract |
| --- | --- |
| Fabric and Quilt | Fabric Transfer API item storage |
| Forge | `IItemHandler` item capability |
| NeoForge 1.20.1 | legacy Forge-compatible item capability |
| NeoForge 1.21.1 | `IItemHandler` item capability |
| NeoForge 26.3 | item `ResourceHandler` capability |

Vanilla `Container` block entities are handled directly. This includes chests,
trapped chests, barrels, and placed shulker boxes. Composite handlers are
deduplicated by the identity exposed by the loader API, and double chests retain
their physical slot ownership without counting one half twice.

An inventory mod should work when its placed block exposes the standard contract
for the active loader and implements simulation consistently. This is an API
compatibility expectation, not a claim that every mod implementing that contract
has been certified. The following exact combinations received bounded runtime
spot checks; see [`release-evidence-0.1.0.md`](release-evidence-0.1.0.md) for
scope and limitations.

| Mod | Minecraft / loader | Runtime result |
| --- | --- | --- |
| Storage Drawers `26.3.0.1+fabric` | 26.3 Fabric | Discovered; withdrawal and insertion exercised without observed duplication. |
| Upgraded Iron Chests `1.5` | 26.3 Forge | Discovered; withdrawal and insertion exercised without observed duplication. |
| Sophisticated Storage `26.3-1.5.124.2189` + Sophisticated Core `26.3-1.5.17.2421` | 26.3 NeoForge | Indexed 17 emeralds; withdrew 8 and returned them, leaving 17 in the physical chest; the count survived save/reopen. |

NeoForge Storage Drawers did not reach an integration test: that mod failed at
startup with `NoSuchFieldError: ModConfig$Type.COMMON`. This is recorded as an
upstream startup incompatibility for that attempted combination, not a
Chestwise compatibility result. These spot checks do not certify other versions
or every implementation of the loader APIs.

## Recipe viewers

The terminal's crafting grid accepts a recipe pushed from a recipe viewer. The
grid is filled from the player's inventory first and topped up from the
surrounding containers, so a recipe transfers even when the ingredients are only
in the chests.

| Viewer | Compile coverage | Runtime startup | Functional transfer | Integration path |
| --- | --- | --- | --- | --- |
| JEI | All nine loader/version nodes | 1.21.1 Fabric, JEI 19.44.0.406: PASS | 1.21.1 Fabric: vanilla transfer/craft, occupied-grid return, full-storage refusal, and insufficient-input refusal PASS. Cake remainders are covered by the vanilla `RecipeManager` GameTest, not a viewer-specific transfer case. Other nodes compile-tested only. | JEI `IRecipeTransferHandler`; Fabric discovers it through `jei_mod_plugin`. |
| EMI | No EMI API/plugin is compiled | 1.21.1 Fabric, EMI 1.1.23+1.21.1+fabric + JEI 19.44.0.406: PASS | Oak-log-to-planks transfer/craft, occupied-grid return, and insufficient-input refusal PASS through JEMI. No native Chestwise EMI plugin. | Indirect JEMI/JEI compatibility (`dev.emi.emi.jemi.JemiPlugin`). |
| REI | Fabric 1.20.1/1.21.1/26.3; Forge 1.20.1; NeoForge 1.20.1/1.21.1/26.3. | Not runtime-tested | Not runtime-tested; compile-tested only | REI `TransferHandler`. |

Chestwise does not compile its REI handler for Forge 1.21.1 or 26.3. Those
combinations are unsupported by this integration matrix; do not infer support
from REI's general project-page loader list.

JEI and REI are compile-only dependencies. None is bundled or required;
Chestwise behaves identically with no recipe viewer installed. EMI is neither
bundled nor declared as a dependency. The tested EMI recipe transfer ran
through EMI's upstream JEMI bridge into Chestwise's JEI transfer handler; this
does not imply a native EMI plugin.

The handlers deliberately read their ingredients from the viewer's slot view
rather than from the recipe object, and send the acceptable item ids for each
slot. That keeps them clear of recipe APIs that differ on every Minecraft
generation and lets a tag ingredient be satisfied by whichever member the
player actually owns. The trade-off is deliberate: recipes whose input depends
on NBT/data components, a required stack count, or a non-item ingredient cannot
be represented faithfully and must not be advertised as supported transfer
targets.

## Recipe-transfer authority and fidelity

Recipe transfer is an inventory-placement intent, not a client-authorized craft.
The packet contains at most nine bounded lists of acceptable item identifiers;
the server validates the open terminal menu, player reach, loaded storage
sources, protected slots, and every withdrawal or deposit. It never accepts an
output stack, a recipe result, or a client-computed crafting result. A malformed
or unknown identifier is a no-op. A client can request an arbitrary *existing*
item identifier, but that grants no capability beyond manually withdrawing that
same item through the already-authorized terminal and placing it into its own
crafting grid.

Recipe identifiers are intentionally not the wire protocol. Viewer APIs do not
provide a stable identifier for every displayed recipe (in particular,
EMI-native recipes), and an identifier alone loses the useful choice among tag
members. The cost is fidelity: viewer transfer supports plain item and tag-like
item alternatives only. It does not support NBT/component predicates, exact
input counts greater than one, or non-item ingredients. This limitation applies
to viewer transfer only; normal terminal crafting is resolved server-side by
Minecraft's `RecipeManager` and retains vanilla ingredient semantics.

The tested EMI+JEI development client displayed EMI's warning counter. During
EMI recipe baking, EMI logged duplicate `jei:/...` IDs after JEMI collected JEI
recipes. This came from the EMI/JEMI/JEI compatibility path, not Chestwise; no
Chestwise exception, class-cast, or linkage error occurred, and the tested
transfers completed. The duplicate-ID report may mean an ambiguous imported
recipe is collapsed by EMI; it does not establish that every imported recipe is
unique or transferable.

## Quilt client and server

The production Fabric artifacts passed dedicated-server startup on Quilt Loader
0.30.0 for Minecraft 1.20.1 and 1.21.1. Client smokes on Quilt Loader 0.30.1
confirmed Chestwise loading, world startup, terminal placement/rendering, screen
open, search input, and Escape close on Minecraft 1.20.1, 1.21.1, and 26.3.
The 26.3 dedicated-server smoke also passed on Loader 0.30.1. These are basic
compatibility smokes, not a claim that every Quilt mod combination has been
tested.

Chestwise never opens item-contained inventories recursively, force-loads a
chunk, accesses another dimension, or treats player backpacks as terminal
storage. Inventory protection/claim mods may deny access through their own
hooks; server operators should test their permission stack before deployment.
