# Chestwise 0.1.0 release evidence

Status: release candidate; not yet approved as a public stable release.

This record captures the automated validation performed on 2026-08-26. It is
deliberately separate from the interactive and publication gates in
[`release-checklist.md`](release-checklist.md).

## Build and behavior tests

- All nine loader/version variants built successfully with compiler warnings
  treated as errors.
- The shared JUnit suite passed on every variant: 18 tests per variant.
- Fabric GameTests passed on all three Minecraft lines: four Chestwise tests on
  1.20.1, four on 1.21.1, and four on 26.2. The 26.2 runner also reported its
  own loader test, for five total.
- The artifact auditor accepted exactly nine release JARs, checked loader
  metadata and version-specific data paths, and generated `SHA256SUMS`.

The GameTests exercise real placed-container discovery, incremental updates,
exact damaged-item variants and withdrawal, deposit matching with protected
slots, removed-container invalidation, and crafting through Minecraft's recipe
manager.

## Packaged server matrix

Each row used the final JAR from `build/release`, a clean server installation,
the listed official loader, and the Java version required by that Minecraft
line. A pass requires the Chestwise initialization marker, Minecraft's `Done`
marker, a requested clean stop, and process exit code zero.

| Minecraft | Loader | Loader version | Java | Result |
| --- | --- | --- | --- | --- |
| 1.20.1 | Fabric | 0.19.3 | 17 | PASS |
| 1.20.1 | Forge | 47.4.23 | 17 | PASS |
| 1.20.1 | NeoForge | 47.1.106 | 17 | PASS |
| 1.21.1 | Fabric | 0.19.3 | 21 | PASS |
| 1.21.1 | Forge | 52.1.16 | 21 | PASS |
| 1.21.1 | NeoForge | 21.1.248 | 21 | PASS |
| 26.2 | Fabric | 0.19.3 | 25 | PASS |
| 26.2 | Forge | 65.1.2 | 25 | PASS |
| 26.2 | NeoForge | 26.2.0.67 | 25 | PASS |

The three packaged Fabric JARs also passed on Quilt Loader 0.30.0 with their
matching Fabric API versions. QSL and QFAPI were not installed.

All supported protocol versions encode container data values as signed 16-bit
fields. Aggregate totals are therefore split into four 16-bit parts and each
received part is masked before the 64-bit value is reconstructed.

Forge and NeoForge 26.2 emitted Log4j appender errors while Netty probed
unsupported native kqueue/epoll transports on Windows. Those loader/runtime
messages did not prevent Chestwise initialization, server readiness, or a clean
exit. The release CI repeats the server matrix on Linux.

## Artifact checksums

```text
55c28bcb59c0a7e17d89e926f92c65f3a4961b7920279eab82c27c46dcdf80ff  chestwise-fabric-1.20.1-0.1.0.jar
ccfc8e47c655192241c4a707ed5496ee87bcd6f1da03981b4d1063ec07115637  chestwise-forge-1.20.1-0.1.0.jar
7187feacd67330cc571443f6400f0fef5e513db11fdfc28a73f7a13b47dbbdeb  chestwise-neoforge-1.20.1-0.1.0.jar
c486c585f21e7fe799607c3425c8c2eedb1826fcf278cc5a75ac025ea058a48d  chestwise-fabric-1.21.1-0.1.0.jar
1cce84c88cec989147f5094b6d22f80a0ba5856c2df7bf80de7a24831c243c16  chestwise-forge-1.21.1-0.1.0.jar
7a039871e6e6fa103fd4dcf5b77ff9fda181aa38260fcd1280c196370c5bb95d  chestwise-neoforge-1.21.1-0.1.0.jar
06e7ae409fa24d3d945fbaeaa33cfcd81f077390306d196561dad7217d50e870  chestwise-fabric-26.2-0.1.0.jar
617d2d7eb2540a808b67b0c2447beb1001c67550a4b8a488ce4d6745989f1945  chestwise-forge-26.2-0.1.0.jar
dc024c118c5c4cd0ecad700d94ba8e29d29123cfd205760e44d7d94f54537ad7  chestwise-neoforge-26.2-0.1.0.jar
```

These hashes describe the current local candidate only. A public release must
be built from the reviewed release tag, and its uploaded files must be compared
with the `SHA256SUMS` produced by that CI run.

## Evidence boundary

Dedicated-server boots prove packaged metadata, entrypoints, registries,
server-side classloading, recipes, loot data, and loader compatibility. They do
not establish client rendering, input ergonomics, concurrent-player behavior,
long-running load stability, or compatibility with a particular third-party
storage mod. Those remain open release gates and must not be inferred from this
record.
