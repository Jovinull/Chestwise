# Changelog

All notable changes are documented here. This project follows semantic
versioning for its own API and behavior; Minecraft-version compatibility is
also encoded in each artifact name.

## 0.1.0 - Unreleased

- Initial public-release candidate.
- Added the Storage Terminal, cached nearby-inventory discovery, exact item
  indexing, search, sorting, withdrawal, deposits, quick stack, protected slots,
  physical location, and 3 × 3 crafting.
- Added Fabric, Forge, and NeoForge artifacts for Minecraft 1.20.1, 1.21.1, and
  26.2, plus Quilt runtime validation of each Fabric artifact.
- Added automated unit tests, Fabric game tests, dedicated-server smoke tests,
  artifact auditing, CI, English and Brazilian Portuguese translations.
- Modelled the Storage Terminal as a vanilla-style catalogue desk with original
  textures, horizontal facing, and a collision shape matching its silhouette.
- The crafting grid and its result now live on the terminal, so a part-built
  recipe survives closing the screen, all open viewers see the same current
  result, and the grid is returned to surrounding containers when the block is
  broken.
- Added optional recipe transfer from JEI and REI, filling the grid from the
  player's inventory first and from surrounding containers after that. EMI
  transfer and crafting were runtime-tested on Fabric 1.21.1 through JEMI/JEI;
  Chestwise has no native EMI plugin.
- Aggregate item counts now draw above the item sprite instead of behind it.
- The crafting result slot no longer overlaps a crafting input slot, and the
  inventory label is no longer hidden underneath a button.
- Escape closes the terminal while the search box has focus.
- Dropping a held stack onto the grid stores it; previously only a shift-click
  would, and sweeping the cursor across the grid could withdraw unasked.
