# Chestwise

> Your storage already is the network.

Chestwise adds one vanilla-friendly Storage Terminal that searches and operates
the physical inventories already present in a storage room. Items remain in
chests, barrels, placed shulker boxes, and loader-compatible modded inventories:
there are no cables, disks, power, storage cells, chunk loading, or virtual
storage.

## Supported platforms

| Minecraft | Fabric | Quilt server smoke | Forge | NeoForge |
| --- | --- | --- | --- | --- |
| 1.20.1 | yes | pass (0.30.0) | yes | yes |
| 1.21.1 | yes | pass (0.30.0) | yes | yes |
| 26.2 | yes | pass (0.30.0) | yes | yes |

Fabric builds require Fabric API. Quilt uses the same Fabric artifact with
Fabric API; QSL and QFAPI are not required. The Quilt evidence is dedicated-
server startup only; client rendering and terminal-screen behavior have not
been certified on Quilt. Forge and NeoForge builds have no runtime library
dependency.

## Features

- Incrementally indexed nearby inventories; never force-loads chunks.
- Exact stack identity across NBT and modern Data Components, with compact
  aggregate counts and exact totals in tooltips.
- Search by name, `@namespace`, or `#tag`, with four sorting modes.
- Server-authoritative withdrawal, deposit matching, deposit all, and quick stack.
- Middle-click protection for player inventory slots.
- Physical item location using coordinates and a short particle marker.
- Integrated vanilla 3 × 3 crafting grid.
- English and Brazilian Portuguese localization.

See the [user guide](docs/user-guide.md), [configuration reference](docs/configuration.md),
[compatibility policy](docs/compatibility.md), and [asset provenance](docs/assets.md).

## Building

The checked-in Gradle wrapper and Java toolchains build all supported variants:

```shell
./gradlew :1.20.1-fabric:build :1.20.1-forge:build :legacy-neoforge-1.20.1:build
./gradlew :1.21.1-fabric:build :1.21.1-forge:build :1.21.1-neoforge:build
./gradlew :26.2-fabric:build :forge-26.2:build :26.2-neoforge:build
python scripts/audit_artifacts.py
```

The final JARs and `SHA256SUMS` are assembled under `build/release`. More detail
is in [testing.md](docs/testing.md).

## License

Chestwise is available under the [MIT License](LICENSE).
