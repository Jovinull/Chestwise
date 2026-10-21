# Chestwise 0.1.0 release-candidate evidence

Status: **not approved for public release**. A bounded two-client multiplayer
test, current-artifact client checks, representative storage-mod tests, and a
short synthetic load run are recorded below. A longer/more diverse soak, full
walkthroughs on every official Fabric line, lifecycle/no-force-loading runtime
coverage, and owner-controlled platform setup remain open.

## Candidate and evidence scope

| Field | Value |
| --- | --- |
| Candidate branch | `release/0.1.0-rc` |
| RC branch base | `b14fa7ee0108235487feb7ebae37cb543ccdec45` |
| Tested code candidate | `c50841fb0b9469ea841c0eceba19cbea58e85624` on `release/0.1.0-rc` |
| Evidence update | This document is reconciled after CI on the tested code candidate; the documentation-only follow-up commit does not change runtime artifacts |
| Evidence date | 2026-10-21 |
| Support matrix | Minecraft 1.20.1, 1.21.1, and 26.3; Fabric, Forge, NeoForge |
| Release channel | Beta; artifact version remains `0.1.0` |

The tested candidate fixes cross-menu storage refresh and middle-click intent
handling, adds regression coverage, and bounds aggregate count labels to the
slot width. A real 1.20.1 Fabric session confirmed the corrected crafting
heading is visible at GUI scale 2. The current committed code candidate passed
Build and test run
[39654906809](https://github.com/Jovinull/Chestwise/actions/runs/39654906809)
and Release run
[39654918799](https://github.com/Jovinull/Chestwise/actions/runs/39654918799).
The Release workflow produced and audited the nine candidate JARs, passed all
official loader and Quilt server smokes, and skipped both publishing jobs.
The CI runs above validate the exact code candidate identified in this report.

## Automated validation

The current local tree completed the following gates:

| Gate | Result | Evidence |
| --- | --- | --- |
| Shared JUnit suites | 378/378; 0 failed, 0 errored, 0 skipped | 42 tests in each of nine variants; 99 JUnit XML suite files |
| Fabric GameTests 1.20.1 | 25/25 | Current local tree; includes cross-open-menu refresh regression |
| Fabric GameTests 1.21.1 | 25/25 | Current local tree; includes cross-open-menu refresh regression |
| Fabric GameTests 26.3 | 26/26 | Current local tree; includes cross-open-menu refresh regression |
| Variant builds | 9/9 | Current local tree; legacy six-node and modern three-node build commands; warnings-as-errors policy retained |
| Artifact audit | 9/9 | Current local tree; `scripts/audit_artifacts.py`; exact nine JARs and regenerated `SHA256SUMS` |
| Download/publisher script tests | 16/16 | `python -m unittest scripts.test_download_utils scripts.test_publish_release` |
| Publisher dry-run | PASS | Nine artifacts validated and 18 platform upload mappings resolved; no network request |
| Official dedicated servers | 9/9 | Release run [39654918799](https://github.com/Jovinull/Chestwise/actions/runs/39654918799) |
| Quilt dedicated servers | 3/3 | Release run [39654918799](https://github.com/Jovinull/Chestwise/actions/runs/39654918799) |
| RC Build and test workflow | PASS, 16/16 jobs | [39654906809](https://github.com/Jovinull/Chestwise/actions/runs/39654906809) |
| RC Release workflow | PASS; package, loader-smoke 9/9, quilt-smoke 3/3; `publish` and `publish-platforms` skipped | [39654918799](https://github.com/Jovinull/Chestwise/actions/runs/39654918799) |

The current-main CI baseline was `Build and test` run
[39256547093](https://github.com/Jovinull/Chestwise/actions/runs/39256547093),
16/16 jobs passed. The RC-specific runs above additionally validate the
modified workflow files and publisher validation on the pushed candidate.

The current-tree build commands were the six legacy `build` tasks and three
26.3 `build` tasks. All nine active JUnit report roots contain 42 tests each,
for 378 tests with no failures, errors, or skips. The three Fabric
`runGameTestServer` tasks were rerun and discovered the totals shown above.
The 26.3 Windows Forge-family server smokes emitted a Log4j appender exception
while Netty probed Linux-only native transports; both loaders fell back, loaded
Chestwise, reached `Done`, and exited cleanly. This is recorded as Windows /
third-party runtime logging noise, not as a Chestwise exception.

## Client and feature runtime evidence

Existing manual evidence in [`testing.md`](testing.md) remains applicable to
the unchanged gameplay code and matching release JARs:

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

On 2026-10-21, the isolated 1.20.1 Fabric client used the current production
artifact, SHA-256
`cfd26ad6821659642d5eb112f854815d6231fa31641afaadbe8ff693903ea0ce`. At GUI
scale 2, the corrected Crafting heading was visible and did not overlap the
mode button. Search for `oak` filtered the list and the tooltip showed the
exact total. Left-click withdrew and returned a 64-item stack; right-click
withdrew 32 and returned one, with the remaining cursor stack deposited back.
Crafting consumed one oak log and produced four planks; the shared grid's 63
remaining logs, recalculated result, and player output survived save, quit,
and reopen. Restock filled a 64-log target and a 64-torch target; 47 torches
were taken from storage while the 17 torches in a protected hotbar slot stayed
untouched. The local launcher logged authentication HTTP 401/profile-key
errors in offline testing; the world itself ran and no Chestwise exception was
found. This is a substantial 1.20.1 session, but not every release control was
retested there.

On 2026-10-21, the isolated 1.21.1 Fabric client used the current production
artifact, SHA-256
`3b0a95e63597c86756e81cc947519bb687c1f2af30e6366be0b3bdc99cc5864c`. It
loaded the disposable world, rendered and opened the terminal, filtered with
`oak`, withdrew eight oak logs, and used Deposit All to return them. The
aggregate count returned to its starting value, and the world was saved and
closed cleanly. This is a current-artifact UI/transfer smoke, not the full
feature walkthrough; a vanilla tutorial toast obscured part of the screen in
this prepared world. No Chestwise exception was found in the log.

A second 1.21.1 Fabric session with that same artifact reopened the prepared
world, filtered the list to 12 diamonds, changed the sort mode to Name, and
saved/closed the world. Search and sort were observed in the live GUI; this
short follow-up did not repeat withdrawal or deposit.

The current 26.3 Forge artifact, SHA-256
`5a6a7290266a35e4efa26661ac56652e7cea710b96c22f407251fec17f3d5302`, loaded
the prepared world, rendered and opened the terminal, and accepted search
field input. The current 26.3 NeoForge artifact, SHA-256
`320ae7fe9f113b7599fa0d452cccd95270d781f23c29e50e2d39e55307c4128d`, loaded
the prepared world, placed and rendered the terminal, opened its screen, and
filtered the storage list with `log`. Both logs show Chestwise discovery and
initialization, with no Chestwise classloading or runtime exception. The Forge
instance also logged two Upgraded Iron Chests mixin-configuration errors and a
RenderPearl minimized-window exception while the game window was minimized;
the client recovered, and none was attributed to Chestwise. These were
current-artifact client/screen smokes, not full feature walkthroughs.

Additional local 26.3 Fabric runtime follow-up on 2026-10-19 used the then-current
production JAR hash `0cb0b5cc0c1e3a13eb7df38833efb0f5439ddfd0c3ed7100074dda46b4a7df32`
in the isolated Prism client and a disposable Fabric
dedicated server bound to `127.0.0.1:25586`. One player client session
placed/opened the terminal, filtered the aggregate view with `arrow`, cleared
the search, and created an arrow target of 64. Starting with one arrow in the
player inventory and 64 in a physical chest, Restock ended with 64 in the
player inventory and one in the chest (63 moved). After `save-all`, graceful
server shutdown, restart, and client reconnect, the target still displayed as
64. The server's Overworld SavedData file was present at
`dimensions/minecraft/overworld/data/chestwise/restock_targets.dat`.

The same session picked up a 64-arrow player stack and deposited it through the
aggregate display; the UI reported 65 available when combined with the one
stored arrow. A right-click withdrawal followed by a right-click carried-item
deposit left 31 on the cursor, which was then placed in an empty player slot.
This exercised the left/right carried-stack paths and cursor placement, but was
not a full capacity-boundary test. This was one player, not multiplayer; after
the second server start the console input was unavailable, so the final physical
inventory count was not independently queried from the server. No Chestwise
exception appeared in the client/server logs. The client had Storage Drawers
installed; its missing hopper-model particle warning and Minecraft's missing
`end_of_frame` post-effect warning were unrelated to Chestwise. The test server
was offline-mode and loopback-only, so it is not evidence of authenticated
multiplayer safety.

A second 26.3 Fabric client session on the same date used that same then-current
production JAR hash and a disposable server bound to `127.0.0.1:25587`. It exercised
`oak` search/clear, crafting one oak log into four planks, and a torch Restock
target of 64: with 16 torches in the player inventory and 64 in a physical
container, Restock moved 48, leaving 64 with the player and 16 in storage.
Quick Stack moved matching bread, arrows, planks, and torches while leaving
unmatched cobblestone and a pickaxe in the player inventory; Deposit All then
moved those remaining stacks. A carried 10-cobblestone stack was deposited to
storage, and right-clicking with five cobblestone moved one while leaving four
on the cursor. Closing the screen returned those four to the player inventory.
After saving, clean server shutdown/restart, and reconnect, the terminal and
physical storage remained available and the torch-64 Restock target appeared in
the target view. No Chestwise exception was observed in the client/server logs.
This was still one player on an offline-mode loopback server, not multiplayer.

On 2026-10-21, two independent clients (RCAlpha and RCBravo) connected to a
disposable 26.3 Fabric dedicated server on loopback using the current artifact
SHA-256 `737329c2e76df7a32da952d5e428be2491fae9e872a5e387d4e6f738d47d1925`.
With 64 iron ingots in a shared chest, close-together withdrawals ended with
64 in RCAlpha, none in RCBravo, and none in the chest. In the deposit/withdraw
scenario, 64 player iron plus 32 chest iron remained 96 total after one client
Deposit Matching and the other withdrew 64 (32 chest, 64 on the receiver,
depositor empty). For Restock, the source had 64 arrows and each player held
one with a target of 64; after both actions, RCAlpha had 64, RCBravo had 2,
and the chest was empty (66 total before and after). RCBravo then disconnected
while its terminal/target UI was open; RCAlpha continued operating without a
lock or Chestwise exception, and the server shut down cleanly. The requests
were processed on the authoritative server thread, not synchronized as a
packet-race stress test. This was offline-mode loopback, not authenticated
public-server testing.

Three real listing screenshots from this 26.3 Fabric client session are
committed at `docs/images/storage-room.png`,
`docs/images/terminal-closeup.png`, and
`docs/images/terminal-gui-crafting.png`. The GUI image shows a live crafting
result and storage/player slots. A separate
[post-restart Restock target capture](images/rc-restock-persistence.png) shows
the target view after reconnect. These captures do not substitute for the
three-line feature walkthrough.

The current 26.3 Forge and NeoForge client startup/screen smokes were completed
in isolated Prism instances: world load, terminal placement/render, item model,
screen open, search input, and close. They were smoke tests, not full feature
walkthroughs.

The release-requested full client walkthrough (withdraw, deposit, craft,
Restock, save/reopen, and log review) has not been performed on all three
official Fabric lines on the current artifacts. The current 1.20.1 session
covered most of that flow; 1.21.1 was a shorter search/withdraw/deposit smoke;
26.3's feature and two-client evidence is described below. The 26.3
Forge/NeoForge runs above were limited startup and screen smokes.

## Multiplayer, lifecycle, performance, and storage compatibility

| Gate | Current evidence | Status |
| --- | --- | --- |
| Two independent players mutating the same source | On 2026-10-21, two separate Prism clients (RCAlpha and RCBravo) connected to a disposable 26.3 Fabric dedicated server in offline-mode loopback. Iron contention conserved 64 total (one player received 64, the other 0, source 0). Deposit Matching versus withdraw conserved 96 (source 32, withdrawing player's cursor 64, depositor 0). Two Restock targets against 64 stored arrows plus one held by each ended at 64 and 2 player arrows, source 0 (66 total). Requests were close together but serialized on the server thread; this was not a deliberately synchronized packet-race test or authenticated public-server test. | PASS — bounded two-client test |
| Disconnect while interacting | One client disconnected with the terminal/target UI open; the other continued withdrawing from the shared source. No lock, Chestwise exception, or stalled menu was observed; server shut down cleanly. Counts were checked for the contention scenarios above, not as a separate disconnect race. | PASS — bounded offline-mode test |
| Container removal, chunk unload/reload, distance | Several invalidation cases have GameTest coverage; no complete real-runtime lifecycle walkthrough recorded | PARTIAL / OPEN |
| Mixed-inventory load/soak near configured maximum | Disposable Forge 26.3 world used 127 barrels (3,429 slots; about 3,425 occupied; about 31 item identities, including damaged swords). Two measured windows totaled 4,115 ticks / 205.71 seconds at about 20 TPS; RSS was 2.86→2.84 GB and private bytes 3.143→3.120 GB. Search, namespace/tag filtering, sorting, withdrawal, Deposit All, and Restock were exercised; no Chestwise exception or obvious tick/memory pathology observed. This was a short synthetic run, not the requested mixed-container/long soak with thousands of distinct identities or concurrent mutations. | PARTIAL / OPEN |
| No force-loading during discovery | During the synthetic run `/forceload query` reported no forced chunks after setup; no chunk-ticket profiler was used and natural unload/reload was not covered | PARTIAL / OPEN |
| Third-party storage contracts | Storage Drawers `26.3.0.1+fabric` on Fabric 26.3; Upgraded Iron Chests `1.5` on Forge 26.3; Sophisticated Storage `26.3-1.5.124.2189` + Core `26.3-1.5.17.2421` on NeoForge 26.3. All three were discovered and exercised for withdrawal/insertion without observed duplication; this is limited spot coverage, not blanket compatibility. The Sophisticated chest held 17 emeralds; Chestwise withdrew 8, returned them, and the 17-item count survived save/reopen. NeoForge Storage Drawers separately failed at startup with upstream `NoSuchFieldError: ModConfig$Type.COMMON`. | PASS — representative contracts only |
| Current-version save/reopen | Forge 26.3 world round trip preserved terminal crafting contents (64 planks), recomputed result, and a 32-torch Restock target. Fabric 26.3 follow-up also preserved the 64-arrow target after dedicated-server restart/reconnect. | PASS for these tested states |
| 26.2 to 26.3 world migration | Exercised during the 26.3 port; see historical port evidence | Prior evidence |
| Uninstall with physical inventories | On a backed-up disposable NeoForge 26.3 world, Chestwise was removed and the copy opened without it. The loader warned about the missing `chestwise:storage_terminal`, skipped that block entity, and reported the terminal item missing from the player inventory. The Sophisticated physical chest still contained all 17 emeralds. | PASS for physical-storage retention; missing Chestwise content is expected |

Only the exact combinations listed above are claimed as runtime-tested. A
NeoForge Storage Drawers startup failure is not attributed to Chestwise: the
upstream mod failed while resolving its config enum before an inventory
integration test could run. The loader-standard contract remains a compatibility
expectation, not certification of all mods that advertise it.

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
the three listing screenshots are captured from the 26.3 Fabric production
client and listed in the draft. The broader cross-version client walkthrough
remains open.

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

The table records the exact production JARs downloaded from the candidate's
Release run [39654918799](https://github.com/Jovinull/Chestwise/actions/runs/39654918799),
the source for eventual tag-based publication. Local builds were manually
smoke-tested; seven local JARs are byte-identical to CI. The local
NeoForge 1.20.1 and Forge 26.3 JARs have different ZIP hashes because local
resource line endings differ and the local Forge JAR includes empty ZIP
directory entries. Their packaged model JSON is equal after newline
normalization; the CI artifacts passed the release artifact audit.

| File | Size (bytes) | SHA-256 |
| --- | ---: | --- |
| `chestwise-fabric-1.20.1-0.1.0.jar` | 155813 | `cfd26ad6821659642d5eb112f854815d6231fa31641afaadbe8ff693903ea0ce` |
| `chestwise-forge-1.20.1-0.1.0.jar` | 159673 | `e38a4345e034f106b31a88f897200331e29fa33ae0a6bf1767960a71016eebe3` |
| `chestwise-neoforge-1.20.1-0.1.0.jar` | 157750 | `601d4a86a61ca779f39bda4082c996fc725fd1d0afff8ce915807aaea5c493bf` |
| `chestwise-fabric-1.21.1-0.1.0.jar` | 157420 | `3b0a95e63597c86756e81cc947519bb687c1f2af30e6366be0b3bdc99cc5864c` |
| `chestwise-forge-1.21.1-0.1.0.jar` | 156566 | `26781b42b5c864abafff9accd8704796f36951d212b801555a6fb6124f567316` |
| `chestwise-neoforge-1.21.1-0.1.0.jar` | 159402 | `2a21c8fc5c6e1d7b85a092ff6628212bf134d38ae6a117bbdb846a231f2487c5` |
| `chestwise-fabric-26.3-0.1.0.jar` | 160677 | `737329c2e76df7a32da952d5e428be2491fae9e872a5e387d4e6f738d47d1925` |
| `chestwise-forge-26.3-0.1.0.jar` | 156423 | `5b7b067779c1d6b37edb697ffff8bba690ca7a21c1f6e9445495c869281d03c0` |
| `chestwise-neoforge-26.3-0.1.0.jar` | 161848 | `320ae7fe9f113b7599fa0d452cccd95270d781f23c29e50e2d39e55307c4128d` |

The artifact audit found no sources, tests/GameTests, fixtures, `.bbmodel`,
viewer APIs, Fabric Loader classes, wrong-loader classes, logs, or temporary
files. Fabric production metadata has no `fabric-gametest` entrypoint.

## Evidence boundary and release decision

Current evidence establishes the automated matrix, package integrity, server
startup, Quilt smoke coverage, the specific client/viewer behaviors, three
representative third-party storage implementations, and limited synthetic load
behavior, and physical-storage retention after Chestwise removal listed above.
It now establishes bounded two-client conservation/disconnect behavior, but
not an authenticated public-server test or a synchronized packet-race test.
It also does not establish a long/diverse mixed-inventory soak, a complete
current-artifact feature walkthrough on every official Fabric line, the full
runtime lifecycle/no-force-loading matrix, or ready-to-publish platform
listings. These remain explicit open gates in
[`release-checklist.md`](release-checklist.md); no public release or tag is
approved by this evidence.
