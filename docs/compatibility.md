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

| Viewer | Compile coverage | Runtime startup | Functional transfer | Integration path |
| --- | --- | --- | --- | --- |
| JEI | All nine loader/version nodes | 1.21.1 Fabric, JEI 19.44.0.406: PASS | 1.21.1 Fabric: vanilla transfer/craft, occupied-grid return, full-storage refusal, and insufficient-input refusal PASS. Cake remainders are covered by the vanilla `RecipeManager` GameTest, not a viewer-specific transfer case. Other nodes compile-tested only. | JEI `IRecipeTransferHandler`; Fabric discovers it through `jei_mod_plugin`. |
| EMI | No EMI API/plugin is compiled | 1.21.1 Fabric, EMI 1.1.23+1.21.1+fabric + JEI 19.44.0.406: PASS | Oak-log-to-planks transfer/craft, occupied-grid return, and insufficient-input refusal PASS through JEMI. No native Chestwise EMI plugin. | Indirect JEMI/JEI compatibility (`dev.emi.emi.jemi.JemiPlugin`). |
| REI | Fabric 1.20.1/1.21.1/26.2; Forge 1.20.1; NeoForge 1.20.1/1.21.1/26.2. | Not runtime-tested | Not runtime-tested; compile-tested only | REI `TransferHandler`. |

Chestwise does not compile its REI handler for Forge 1.21.1 or 26.2. Those
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
0.30.0 for Minecraft 1.20.1, 1.21.1, and 26.2. Client smoke tests then used the
same Fabric artifacts on Quilt Loader 0.30.1 for all three versions: Chestwise
loaded, a world opened, the terminal was obtained and placed, its model rendered,
the screen opened, search accepted input, and Escape closed it. These are client
and server compatibility smokes, not a claim that every Quilt mod combination
has been tested.

Chestwise never opens item-contained inventories recursively, force-loads a
chunk, accesses another dimension, or treats player backpacks as terminal
storage. Inventory protection/claim mods may deny access through their own
hooks; server operators should test their permission stack before deployment.
