# Release checklist

No release may be called stable solely because it compiles. Check each item
only after evidence exists; the current status and evidence boundaries are in
[`release-evidence-0.1.0.md`](release-evidence-0.1.0.md).

## Automated gates

- [x] Clean-checkout builds pass for all nine Minecraft/loader variants with
  warnings treated as errors.
- [x] Shared JUnit suites pass on all nine variants (369/369; no skipped tests).
- [x] Fabric GameTests pass on Minecraft 1.20.1, 1.21.1, and 26.3.
- [x] Artifact audit accepts exactly nine production JARs, excludes test code,
  and emits `SHA256SUMS`.
- [x] Official dedicated-server smoke passes for all nine artifacts.
- [x] Quilt dedicated-server and basic client smokes pass for the three Fabric
  artifacts; this is not a claim of general Quilt-mod compatibility.
- [x] Bounded-download and publishing-validation script tests pass (16/16); the
  offline publishing dry-run resolves nine artifacts and eighteen mappings.
- [x] Run the updated RC `Build and test` and `Release` workflows on the pushed
  RC branch; release dispatch skipped publishing (runs 39282152711 and
  39282161012).

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
- [ ] Smoke the current 26.3 Forge and NeoForge clients through world load,
  terminal placement, rendering, and screen open.
- [ ] Exercise the remaining mouse/keyboard controls and full-inventory,
  protected-slot, and Deposit Matching/Deposit All cases on the candidate.
- [ ] Capture and review the three actual listing screenshots described in
  [`release-listing-0.1.0.md`](release-listing-0.1.0.md).

## Multiplayer, lifecycle, performance, and compatibility gates

- [ ] Run two independent clients against one dedicated server and shared
  terminal/source; verify concurrent withdraw, Deposit Matching, and Restock
  conserve items, including a disconnect during interaction.
- [ ] Complete a runtime lifecycle walkthrough for chest/barrel removal,
  double-chest changes, chunk unload/reload, external source mutation, terminal
  removal, and player distance; verify no force-loading or stale mutation.
- [ ] Run a disposable-world soak with up to 128 mixed inventories, thousands
  of variants, repeated search/sort/transfers/Restock, and external mutations;
  record tick behavior, pauses, errors, and memory trend.
- [ ] Validate at least one real third-party storage implementation for each
  Fabric Transfer API, Forge handler, and NeoForge handler contract. Do not
  name an untested mod as compatible.
- [ ] Save and reopen a current 26.3 candidate world containing a terminal,
  shared crafting contents, and Restock targets.
- [x] Preserve the previously exercised 26.2-to-26.3 world migration evidence;
  it is documented in the 26.3 port record.
- [ ] On a backed-up disposable world, remove Chestwise and observe loader/
  vanilla behavior. Confirm physical chest/barrel contents remain ordinary
  world data; document any missing-block limitations.

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
