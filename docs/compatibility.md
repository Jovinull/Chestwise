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

Chestwise never opens item-contained inventories recursively, force-loads a
chunk, accesses another dimension, or treats player backpacks as terminal
storage. Inventory protection/claim mods may deny access through their own
hooks; server operators should test their permission stack before deployment.
