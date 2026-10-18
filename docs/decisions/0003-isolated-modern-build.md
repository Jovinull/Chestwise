# 0003: Isolated build for Minecraft 26.3

- Status: accepted for the 26.3 port
- Date: 2026-10-18

## Context

Minecraft 26.3 is an unobfuscated modern target and requires newer build
tooling than the 1.20.1/1.21.1 line. Updating the root Stonecraft/Loom stack
would also change the environment that runs the established NeoForge 1.21.1
JUnit setup; Stonecraft 1.14.x has a known `mainargs.txt` regression there.
Forge 26.3 additionally uses the official ForgeGradle 7 MDK path rather than
the Loom-backed Forge nodes used by the older Minecraft lines.

## Decision

Keep the 1.20.1/1.21.1 Stonecutter build on its known-good root toolchain.
Build Fabric and NeoForge 26.3 in `platforms/modern-26.3` with Stonecraft
1.14.5, Stonecutter 0.9.8, and Loom 1.17. Build Forge 26.3 in the isolated
`platforms/forge-26.3` subproject using the official ForgeGradle 7 setup.
All three use the shared source tree, with only loader-specific bootstrap and
storage adapters copied into their normal bridge boundaries. The generated
source copy and caches are ignored; the canonical 26.3 dependency pins are
checked against the isolated build's copy before generation.

The production matrix remains nine artifacts: the six 1.20.1/1.21.1 cells and
the three 26.3 cells. Minecraft 26.2 is replaced, not retained alongside 26.3.

## Consequences

- The 26.3 build must be invoked with `./gradlew -p platforms/modern-26.3`.
- CI, release packaging, test instructions, and artifact auditing explicitly
  include tasks and paths from both Gradle roots.
- Root Stonecraft can be upgraded later only after revalidating the legacy
  NeoForge test launcher; this port does not silently couple that upgrade to
  Minecraft support changes.
- Quilt uses the 26.3 Fabric JAR and remains a separate runtime smoke, not a
  fourth artifact.
