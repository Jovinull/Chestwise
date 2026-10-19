# Chestwise 0.1.0 release-candidate evidence

Status: **not approved for public release**. Required multiplayer, load/soak,
third-party storage, uninstall, and final listing-asset gates remain open.

## Candidate and evidence scope

| Field | Value |
| --- | --- |
| Candidate branch | `release/0.1.0-rc` |
| Game/source baseline | `b14fa7ee0108235487feb7ebae37cb543ccdec45` |
| Evidence date | 2026-10-19 |
| Support matrix | Minecraft 1.20.1, 1.21.1, and 26.3; Fabric, Forge, NeoForge |
| Release channel | Beta; artifact version remains `0.1.0` |

The RC-only changes at this point are release workflows, packaging/publishing
validation, tests for those scripts, and release documentation. No gameplay,
runtime Java, or shipped resource code changed from the source baseline above.
The clean build was performed in a detached clean worktree at that commit; its
nine JARs are byte-identical to the JARs downloaded from the successful `main`
Release workflow. The RC branch workflows still require their own run after the
branch is pushed; those results must be appended before final review.

## Automated validation

The clean checkout completed the following gates:

| Gate | Result | Evidence |
| --- | --- | --- |
| Shared JUnit suites | 369/369; 0 failed, 0 errored, 0 skipped | 99 JUnit XML suite files across nine variants |
| Fabric GameTests 1.20.1 | 24/24 | Clean-worktree `runGameTestServer` |
| Fabric GameTests 1.21.1 | 24/24 | Clean-worktree `runGameTestServer` |
| Fabric GameTests 26.3 | 25/25 | Clean-worktree modern-node `runGameTestServer` |
| Variant builds | 9/9 | Clean-worktree release build commands; warnings-as-errors policy retained |
| Artifact audit | 9/9 | `scripts/audit_artifacts.py`; exact nine JARs and `SHA256SUMS` |
| Download/publisher script tests | 16/16 | `python -m unittest scripts.test_download_utils scripts.test_publish_release` |
| Publisher dry-run | PASS | Nine artifacts validated and 18 platform upload mappings resolved; no network request |
| Official dedicated servers | 9/9 | Main Release workflow run [39257448570](https://github.com/Jovinull/Chestwise/actions/runs/39257448570) |
| Quilt dedicated servers | 3/3 | Same Release workflow run; exact artifact hashes listed below |

The current-main CI baseline was `Build and test` run
[39256547093](https://github.com/Jovinull/Chestwise/actions/runs/39256547093),
16/16 jobs passed. The current-main Release run linked above passed and skipped
publishing. The RC workflow changes (including the new publisher validation)
are not covered by those historical workflow runs and must be checked on the RC
branch.

Clean-worktree commands were the six legacy `build` tasks, the three 26.3
`build` tasks, all nine `test` tasks, and the three Fabric
`runGameTestServer` tasks. The modern unit tasks were explicitly rerun; the
aggregate XML result was 369 tests with no failures, errors, or skips.

## Client and feature runtime evidence

Existing manual evidence in [`testing.md`](testing.md) remains applicable to
the unchanged game code and matching release JARs:

- Minecraft 1.21.1 Fabric: terminal GUI at Auto/2/3 scales; search filtering,
  clearing and focus; ESC and inventory-key behavior; tooltips, counters,
  labels, and representative slot hitboxes; carried-stack full/partial/full-
  storage/right-click/close behavior; Restock add/edit/remove and save/reopen.
- Minecraft 1.21.1 Fabric + JEI 19.44.0.406: vanilla transfer and craft,
  occupied-grid return, capacity refusal, and insufficient-input refusal.
- Minecraft 26.3 Fabric + JEI 31.16.0.83: plugin discovery, physical storage
  ingredient transfer, and actual craft.
- EMI 1.1.23 on 1.21.1 Fabric used the JEMI/JEI bridge; startup, transfer,
  craft, occupied-grid, and insufficient-input cases passed.
- Quilt client basic smokes used the production Fabric artifact on 1.20.1,
  1.21.1, and 26.3: world load, terminal placement/render, screen open, search
  input, and Escape close. These do not certify the full feature walkthrough.

The release-requested full client walkthrough (withdraw, deposit, craft,
Restock, save/reopen, and log review) has not been performed on all three
official Fabric lines. Current RC Forge and NeoForge client screens have not
been smoke-tested. No listing screenshots have been captured from the actual
candidate client.

## Multiplayer, lifecycle, performance, and storage compatibility

| Gate | Current evidence | Status |
| --- | --- | --- |
| Two independent players mutating the same source | No real two-client dedicated-server session performed | OPEN release blocker |
| Disconnect while interacting | Not tested in a two-client session | OPEN |
| Container removal, chunk unload/reload, distance | Several invalidation cases have GameTest coverage; no complete real-runtime lifecycle walkthrough recorded | PARTIAL / OPEN |
| 128-inventory load/soak with thousands of variants | No soak run or measurements recorded | OPEN release blocker |
| No force-loading during discovery | No explicit observed chunk-boundary soak evidence recorded | OPEN |
| Third-party storage contracts | No named third-party storage implementation was runtime-tested | OPEN release blocker |
| Current-version save/reopen | Restock persistence was observed on 1.21.1 Fabric; no complete 26.3 candidate-world round trip recorded | PARTIAL |
| 26.2 to 26.3 world migration | Exercised during the 26.3 port; see historical port evidence | Prior evidence |
| Uninstall with physical inventories | No backed-up-world removal test recorded | OPEN release blocker |

No specific Storage Drawers, Sophisticated Storage, Iron Chests, or other
third-party mod is claimed as tested. Compatibility documentation describes
loader-standard inventory contracts, not certification of named mods.

## Recipe viewers

| Viewer | Combination | Evidence |
| --- | --- | --- |
| JEI 19.44.0.406 | 1.21.1 Fabric | Runtime startup and functional transfer/craft plus relevant transfer edge cases |
| JEI 31.16.0.83 | 26.3 Fabric | Runtime startup/plugin discovery and physical-storage transfer/craft |
| JEI | Other documented nodes | Compile-tested only |
| EMI 1.1.23 + JEI 19.44.0.406 | 1.21.1 Fabric | Runtime tested via JEMI/JEI bridge; no native Chestwise EMI plugin |
| REI | Documented nodes in `compatibility.md` | Compile-tested only; no runtime-transfer claim |

Cake remainder behavior is covered by the vanilla RecipeManager GameTest, not
independently attributed to recipe-viewer transfer.

## Project identity and distribution readiness

An exact-name web search on 2026-10-19 found no other Minecraft mod project
named Chestwise. This is a search result, not a legal trademark clearance.
Modrinth search showed no existing Chestwise mod listing and the exact slug
check returned 404, which does not reserve the slug. CurseForge availability
could not be confirmed because its page endpoint returned HTTP 403 in this
environment. Neither platform project has been created or reserved.

The GitHub source/issue URL is `https://github.com/Jovinull/Chestwise`. The
listing draft and release notes are in
[`release-listing-0.1.0.md`](release-listing-0.1.0.md) and
[`release-notes-0.1.0.md`](release-notes-0.1.0.md). The actual mod icon exists;
the three required listing screenshots are not yet captured.

The Release workflow packages nine artifacts and has an offline publishing
mapping validator. Its publisher validates exact paths, checksums, embedded
metadata, per-file game/loader mappings, and Fabric API dependency assignment
before any network request. The dry-run passed for all nine artifacts and 18
platform mappings. Publishing credentials and project IDs are absent. The
owner must create/reserve the Modrinth and CurseForge projects, configure
`CHESTWISE_MODRINTH_PROJECT_ID`, `CHESTWISE_CURSEFORGE_PROJECT_ID`,
`MODRINTH_TOKEN`, and `CURSEFORGE_TOKEN`, and approve the live staged upload.
CurseForge slug availability and live platform permissions remain unverified.

The workflow only publishes on a push of the exact `v0.1.0` tag; a
`workflow_dispatch` on a branch or tag skips all publishing jobs. The selected
channel is Beta/Prerelease while the version remains `0.1.0`. No signing setup
has been verified; after approval, use an annotated tag and verify its target
SHA before pushing it.

## Production artifacts

These are the nine release JARs from the clean build. Each hash matched the
corresponding file downloaded from the successful main Release workflow.

| File | Size (bytes) | SHA-256 |
| --- | ---: | --- |
| `chestwise-fabric-1.20.1-0.1.0.jar` | 154434 | `98c73ae6357fa49592d096a6a0ffc9866f12f8d42842e3372ccbf15b0cfee79c` |
| `chestwise-forge-1.20.1-0.1.0.jar` | 158283 | `9aa933b41dddb8638428b8f6e0216b24228959cc57474c44e7a59555bf8bd8a6` |
| `chestwise-neoforge-1.20.1-0.1.0.jar` | 156384 | `129cb1742f3ad3014755c83c1c2b664916275b9014f307588060ffbc9d328a94` |
| `chestwise-fabric-1.21.1-0.1.0.jar` | 156020 | `6977c3a6bbebe226446fee8c77864aa6ad951ac34cec72f387f3d9494f409155` |
| `chestwise-forge-1.21.1-0.1.0.jar` | 155171 | `ff7e0f04035696f10f374d3c79777f302d33aea708b4d1cb929082122f141ff2` |
| `chestwise-neoforge-1.21.1-0.1.0.jar` | 158007 | `70e83de5dee1597ec9777dc82003cbdeec55c0146116897702db37541909ab1f` |
| `chestwise-fabric-26.3-0.1.0.jar` | 159239 | `2ec175457101003df8a3f2aaeb8fa9eb5967a9e2a5526b5ead8f2496193eb724` |
| `chestwise-forge-26.3-0.1.0.jar` | 154985 | `e4deab98f56a72d5713ee5017e51e5eec865cf63180f878a8d44d1a8ab4afe22` |
| `chestwise-neoforge-26.3-0.1.0.jar` | 160410 | `efec1f41f845a57cb3f1c7083794983d80b86c02be57097fbc3fdaf868f4ddc9` |

The artifact audit found no sources, tests/GameTests, fixtures, `.bbmodel`,
viewer APIs, Fabric Loader classes, wrong-loader classes, logs, or temporary
files. Fabric production metadata has no `fabric-gametest` entrypoint.

## Evidence boundary and release decision

Current evidence establishes the automated matrix, package integrity, server
startup, Quilt smoke coverage, and the specific client/viewer behaviors listed
above. It does not establish real multiplayer conservation, a 128-inventory
soak, third-party storage compatibility, uninstall behavior, all-line full
client walkthroughs, or ready-to-publish platform listings. Those are explicit
open gates in [`release-checklist.md`](release-checklist.md); no public release
or tag is approved by this evidence.
