# 0002: Multiversion and multiloader architecture

- Status: accepted
- Date: 2026-08-25

## Decision

Use Gradle with Stonecutter and Stonecraft. Stonecraft currently composes
Stonecutter with Architectury Loom and supports Fabric, Forge, NeoForge,
Mojmap, the unobfuscated 26.x line, and Java 17/21/25. These are build-time
tools only. Chestwise does not require Architectury API at runtime.

One preprocessed source tree produces nine real loader/version nodes. Quilt
server compatibility is smoke-tested by booting each Fabric artifact with
Quilt Loader; client compatibility remains separately unverified. Quilt is
not a tenth source fork.

Business code owns its abstractions (`StorageSource`, `StorageView`,
`StorageSlot`, `StorageAdapter`, and `StorageDiscovery`). Loader APIs are only
referenced by adapters and bootstrap bridges.

## Alternatives considered

- Four copied implementations: rejected because fixes would drift.
- Architectury API runtime dependency: rejected because registration and
  networking conveniences do not justify another required user mod.
- The current official MultiLoader Template: useful reference, but it only
  models Fabric and NeoForge and does not solve three Minecraft generations.
- Independent Gradle trees: workable, but multiplies metadata, CI, and source
  divergence.

## Compatibility exception

NeoForge 1.20.1 predates the modern `net.neoforged:neoforge` coordinate and is
published as `net.neoforged:forge`. Architectury Loom's `neoForge()` path only
supports the newer userdev format and cannot consume this release. That single
cell is therefore a small, explicit ModDevGradle Legacy subproject configured
with `neoForgeVersion`; it shares core sources but owns its bootstrap and
metadata. This is an upstream format boundary, not a fourth gameplay fork.

Forge 26.2 is another upstream build-format boundary. Architectury Loom 1.14
cannot configure its unobfuscated userdev (it executes zero MCP steps and fails
inside setup), while the official Forge 26.2 MDK uses ForgeGradle 7. That cell
is an equally small ForgeGradle subproject following the official MDK.

Support remains provisional until each built jar boots under its target loader.
