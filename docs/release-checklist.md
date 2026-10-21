# Release checklist

No release may be called stable solely because it compiles. Check each item
only after evidence exists; the current status and evidence boundaries are in
[`release-evidence-0.1.0.md`](release-evidence-0.1.0.md).

## Automated gates

- [x] Clean-checkout builds pass for all nine Minecraft/loader variants with
  warnings treated as errors.
- [x] Shared JUnit suites pass on all nine variants (378/378; no skipped tests).
- [x] Fabric GameTests pass on Minecraft 1.20.1, 1.21.1, and 26.3.
- [x] Artifact audit accepts exactly nine production JARs, excludes test code,
  and emits `SHA256SUMS`.
- [x] Official dedicated-server smoke passes for all nine artifacts.
- [x] Quilt dedicated-server and basic client smokes pass for the three Fabric
  artifacts; this is not a claim of general Quilt-mod compatibility.
- [x] Bounded-download and publishing-validation script tests pass (16/16); the
  offline publishing dry-run resolves nine artifacts and eighteen mappings.
- [ ] Run `Build and test` and `Release` on the current committed RC state;
  publishing must remain skipped. Earlier RC runs 39282152711 and 39282161012
  passed before the current menu/count fixes and do not certify this candidate.

The clean build, GameTests, hashes, and RC workflow runs are recorded in
[`release-evidence-0.1.0.md`](release-evidence-0.1.0.md).

## Client and interactive gates

- [x] Basic client startup, world load, terminal placement/render, screen open,
  search input, and Escape close have evidence on all three Minecraft lines
  using the production Fabric artifact under Quilt.
- [x] A detailed Minecraft 1.21.1 Fabric session covered GUI scales Auto/2/3,
  search, focus, tooltips, counters, labels, representative hitboxes,
  carried-stack deposit edge cases, and Restock persistence.
- [x] JEI functional transfer/crafting has runtime evidence on 1.21.1 Fabric
  and 26.3 Fabric; EMI used the JEMI/JEI bridge on 1.21.1 Fabric. REI remains
  compile-tested only where listed in `compatibility.md`.
- [ ] Complete the release walkthrough on official Fabric clients for 1.20.1,
  1.21.1, and 26.3: open, search, withdraw, deposit, craft, Restock, save,
  reopen, and inspect logs.
- Current-artifact spot checks on 2026-10-21 exercised the 1.20.1 Fabric UI,
  search, transfer/crafting, Restock, and save/reopen, plus 1.21.1 Fabric world
  load, search, withdrawal, Deposit All, and save/quit. They do not satisfy the
  complete three-line walkthrough above.
- [x] Smoke the current 26.3 Forge and NeoForge clients through world load,
  terminal placement, rendering, and screen open; these were startup/screen
  smokes, not full feature walkthroughs.
- [ ] Exercise the remaining mouse/keyboard controls and full-inventory,
  protected-slot, and Deposit Matching/Deposit All cases on the candidate.
- [x] Capture and review the three actual listing screenshots described in
  [`release-listing-0.1.0.md`](release-listing-0.1.0.md). They are real 26.3
  Fabric RC client captures; this does not close the other client walkthroughs.

## Multiplayer, lifecycle, performance, and compatibility gates

- [x] Run two independent clients against one dedicated server and shared
  terminal/source; verify withdraw contention, Deposit Matching vs withdraw,
  Restock contention, and a disconnect during interaction. Counts and the
  offline-mode/loopback scope are recorded in the release evidence; this is not
  an authenticated public-server test.
- [ ] Complete a runtime lifecycle walkthrough for chest/barrel removal,
  double-chest changes, chunk unload/reload, external source mutation, terminal
  removal, and player distance; verify no force-loading or stale mutation.
- [ ] Run a disposable-world soak with up to 128 mixed inventories, thousands
  of variants, repeated search/sort/transfers/Restock, and external mutations;
  record tick behavior, pauses, errors, and memory trend.
- [x] Validate one real third-party storage implementation for each Fabric
  Transfer API, Forge handler, and NeoForge handler contract. Exact tested
  combinations and limitations are in [`compatibility.md`](compatibility.md)
  and [`release-evidence-0.1.0.md`](release-evidence-0.1.0.md); this is not a
  blanket compatibility claim.
- [x] Save and reopen a current 26.3 candidate world containing a terminal,
  shared crafting contents, and Restock targets. Forge 26.3 preserved 64
  planks, recalculated the result, and retained a 32-torch target; Fabric 26.3
  also retained an arrow target across dedicated-server restart/reconnect.
- [x] Preserve the previously exercised 26.2-to-26.3 world migration evidence;
  it is documented in the 26.3 port record.
- [x] On a backed-up disposable NeoForge 26.3 world, remove Chestwise and
  confirm physical Sophisticated Storage contents remain. The loader reports
  and skips the missing Chestwise terminal block/entity and held item; this is
  not a guarantee of consequence-free mod removal.

## Publication gates

- [x] Recheck the exact Chestwise name; no exact competing Minecraft project
  was found in the 2026-10-19 search. This is not trademark clearance.
- [ ] Confirm and reserve the `chestwise` Modrinth and CurseForge project slugs;
  Modrinth showed no listing but is not reserved, and CurseForge availability
  remains unverified.
- [x] Confirm the nine candidate JAR metadata mappings, repository links,
  Minecraft ranges, loader labels, Fabric API requirement, beta channel, and
  checksums with the offline publisher dry-run and artifact audit.
- [ ] Create/review both platform project pages and configure repository
  variables `CHESTWISE_MODRINTH_PROJECT_ID`,
  `CHESTWISE_CURSEFORGE_PROJECT_ID` and secrets `MODRINTH_TOKEN`,
  `CURSEFORGE_TOKEN`.
- [ ] Perform an authorized staging test with the project owner's platform
  credentials, then verify uploaded hashes against that workflow's
  `SHA256SUMS`.
- [ ] After final approval only, replace `Unreleased` in `CHANGELOG.md` with
  the release date.
- [ ] Sign/tag the exact reviewed commit as `v0.1.0` (or use a verified
  annotated tag if signing is not configured) and confirm tag CI builds the
  published assets.
- [ ] Compare the GitHub, Modrinth, and CurseForge files against the same CI
  `SHA256SUMS` and publish the Beta listing.

Do not create a release tag, publish platform versions, or describe 0.1.0 as
stable until all required open gates have evidence and the exact release commit
has been approved.
