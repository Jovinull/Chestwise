# Chestwise 0.1.0 listing draft

Release channel: **Beta**. Keep the mod version and JAR version at `0.1.0`;
the platform's beta channel communicates that this is the first public build
and has not yet had broad field exposure.

## Short description

Your storage already is the network. Search, move, craft, and restock items
across the chests you already use—with no cables, power, disks, or virtual
storage.

## Description

Chestwise adds one vanilla-friendly Storage Terminal to your storage room.
Place it near chests, barrels, placed shulker boxes, and compatible modded
inventories to search and move their real contents from one screen. Items stay
in those physical inventories; Chestwise does not create remote or virtual
storage.

Use the terminal to search by item, mod, or tag; deposit matching items or
quick-stack supplies; locate an item's physical container; craft from nearby
storage; and restock personal item targets. No cables, energy, disks, channels,
or chunk loading.

### Supported versions and loaders

- Minecraft 1.20.1, 1.21.1, and 26.3.
- Fabric, Forge, and NeoForge artifacts for each listed Minecraft version.
- Fabric artifacts require Fabric API. The same Fabric artifact has passed
  basic Quilt client/server smokes on all three supported lines; QSL and QFAPI
  are not required.
- Recipe viewers are optional. Consult the compatibility notes for the exact
  runtime-tested integrations.

### Links

- Source and issue tracker: <https://github.com/Jovinull/Chestwise>
- User guide: <https://github.com/Jovinull/Chestwise/blob/main/docs/user-guide.md>
- Compatibility: <https://github.com/Jovinull/Chestwise/blob/main/docs/compatibility.md>
- Bug reports: <https://github.com/Jovinull/Chestwise/issues>

## Listing screenshots

These are unedited screenshots from the actual Minecraft 26.3 Fabric RC client
using a production Chestwise JAR. The world was a disposable server-prepared
storage room; no UI was mocked or composited. The first two images are room
views at different distances, and the third shows the live terminal GUI.

1. [Storage room](images/storage-room.png) — Terminal with ordinary barrels,
   crafting table, and room context.
2. [Terminal close-up](images/terminal-closeup.png) — block model and adjacent
   physical storage.
3. [Terminal GUI and crafting](images/terminal-gui-crafting.png) — aggregated
   storage items, crafting input/result, and player inventory.

These assets are captured and reviewed for the listing draft. They document
26.3 Fabric only; the broader release walkthrough on all official Fabric
lines remains a separate gate.
