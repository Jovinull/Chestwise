# Release checklist

No release may be called stable solely because it compiles. Check each item and
attach logs or CI URLs to the release issue.

## Automated gates

- [x] All nine artifact builds pass with compiler warnings treated as errors.
- [x] All shared JUnit suites pass on all nine variants.
- [x] Fabric game tests pass on Minecraft 1.20.1, 1.21.1, and 26.2.
- [x] The three packaged Fabric JARs boot and stop cleanly on Quilt Loader.
- [x] Every packaged loader JAR boots and stops cleanly on its official dedicated server.
- [x] `python scripts/audit_artifacts.py` reports nine passes and emits `SHA256SUMS`.

Local evidence and its precise scope are recorded in
[`release-evidence-0.1.0.md`](release-evidence-0.1.0.md). Release CI repeats all
automated gates from a tag before it can publish assets.

## Interactive gates

- [ ] Client opens the terminal on all three Minecraft lines without rendering errors.
- [ ] Search, paging, each sort mode, tooltips, crafting, and keyboard focus are exercised.
- [ ] Left/right/shift/middle-click and opt-in wheel behavior are exercised.
- [ ] Deposit Matching, Deposit All, protected slots, and full-inventory fallback are exercised.
- [ ] Two concurrent players exercise the same terminal and mutate the same source inventory.
- [ ] Container removal, chunk unload/reload, terminal removal, and player distance invalidation are exercised.
- [ ] A disposable world runs a load/soak test with 128 mixed inventories and thousands of variants.
- [ ] At least one representative third-party storage implementation per loader contract is tested.
- [ ] Upgrade and uninstall are tested on a backed-up world; no item data is owned by the terminal.

## Publication gates

- [ ] Re-run the project-name collision check and reserve repository/platform slugs.
- [ ] Replace `Unreleased` in `CHANGELOG.md` with the release date.
- [ ] Confirm metadata links point to the real public repository.
- [ ] Sign/tag the exact reviewed commit and let CI build from that tag.
- [ ] Compare uploaded assets against `SHA256SUMS` and publish loader/dependency requirements.
