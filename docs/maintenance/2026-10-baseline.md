# October 2026 maintenance baseline

This note records the maintenance branch cut from `main` on 2026-10-18. It does not merge or change `main`.

## Starting point and pre-change baseline

- Repository: `Jovinull/Chestwise`
- Main/maintenance starting commit: `b951e33838470d64560e9eb9469b64cea5fd9fc2`
- Historical main CI: [Build and test, run 37204800044](https://github.com/Jovinull/Chestwise/actions/runs/37204800044), 15/15 jobs passed.
- Unit-test executions: 369, 0 failed, errored, or skipped.
- Fabric GameTests: 1.20.1 24/24; 1.21.1 24/24; 26.2 25/25.
- Builds: all nine Minecraft/loader variants passed.
- Production artifact audit: 9/9 passed.

Those are the baseline results before the maintenance edits. The maintenance branch subsequently repeated a clean build of all nine variants and the three Fabric GameTest tasks; all passed. Its final local artifact audit also passed 9/9. Local dependency resolution for that final matrix used the already populated Gradle cache because the local Shedaniel endpoint timed out during a direct online attempt; the pushed branch workflows are the online-resolution check.

The maintenance branch's online verification at `f434ef0788c075444b837697e171a1f3acf9f998` completed successfully: [Build and test run 39130546536](https://github.com/Jovinull/Chestwise/actions/runs/39130546536) passed 16/16 jobs, and [Release run 39130549206](https://github.com/Jovinull/Chestwise/actions/runs/39130549206) passed its package job, nine loader server smokes, and three Quilt server smokes (13 jobs passed; tag-only publishing was skipped). The package job built all nine variants online, ran all three Fabric GameTest suites (24/24, 24/24, 25/25), and audited all nine production jars.

## Dependabot resolver failure (separate from PR build checks)

The actual [Dependabot Updates run 38625457933](https://github.com/Jovinull/Chestwise/actions/runs/38625457933), job `116063354157`, ran on 2026-10-14 against the starting `main` commit. The updater summary called two failures `private_source_authentication_failure`, for `org.junit:junit-bom` and `net.fabricmc:fabric-loader`, and attributed both to `https://maven.blamejared.com`.

The proxy log establishes a different cause. Requests such as

```text
https://maven.blamejared.com:443/org/junit/junit-bom/maven-metadata.xml
```

were rejected with `403 Forbidden` immediately after `egress not allowlisted maven.blamejared.com`. The same proxy denial occurred for `maven.shedaniel.me` and `maven.fabricmc.net`; the job log does not show a `401`, credential challenge, redirect, or an upstream response from BlameJared for these denied requests. The requested JUnit group is resolved from Maven Central, and the Fabric Loader group belongs to Fabric's Maven, not BlameJared. This was broad repository probing by the updater, not evidence that either artifact is private or that BlameJared authentication is required.

Independent endpoint checks returned HTTP 200 for the public BlameJared root and a real JEI POM, HTTP 404 for the wrong-owner JUnit metadata path on BlameJared, and HTTP 200 for JUnit metadata on Maven Central. These responses had no redirect. The normal main `Build and test` run was green; local Gradle resolved the graph from its cache and, where reachable, the public repositories. Dependabot ran inside its own updater container and egress proxy, whose allowlist denied the custom hosts. Its log also says `Secret source: None`; only GitHub source credentials were configured. There is no basis for adding Maven credentials.

Gradle dependency repositories are now content-filtered by the artifact groups actually used: BlameJared for `mezz.jei`; Shedaniel for REI groups and its observed `dev.architectury` transitives; Fabric Maven for `net.fabricmc` groups. Maven Central is declared first where those project repositories are configured. The legacy NeoForge and Forge 26.2 subprojects retain their own declarations because they are isolated Gradle build boundaries. Plugin-management repositories remain broad because filtering them by plugin-marker group broke Loom's Mojang/MCP transitives. These filters reduce unnecessary Gradle requests and preserve observed transitive coordinates; they are not claimed to repair Dependabot's resolver proxy. No `registries:` block, credentials, or secrets were added. The branch workflow is the available verification of normal Gradle resolution; Dependabot's hosted resolver still needs GitHub-side egress allowlisting or an upstream resolver fix.

## Dependency and workflow decisions

| Dependency/update | Previous | Evaluated | Branch decision | Evidence / disposition |
| --- | --- | --- | --- | --- |
| `actions/checkout` (#2) | v6 | v7 | Replace | Current branch Build and Release workflows use v7; stale PR failures came from the branch's non-executable Gradle wrapper, not checkout. |
| `actions/setup-java` (#7) | v5 | v6 | Replace | Current Java 17/21/25 matrix and Release workflow use v6; stale PR failures predated the Java 25 workflow correction. |
| `actions/upload-artifact` (#16) | v4 | v6 | Replace | Dependabot PR checks were green; branch workflows use v6. |
| `actions/download-artifact` (#15) | v5 | v7 | Replace | Dependabot PR checks were green; branch workflows use v7. |
| Gradle wrapper (#17) | 9.7.0 | 9.8.1 | Replace | Wrapper task generated the standard wrapper files; all nine builds and tests passed locally. Its old Quilt smoke failure was an upstream Quilt Meta HTTP 503, now handled with bounded retries. |
| Fabric Loader (#6) | 0.19.3 runtime / hard-coded 0.16.9 compile-only | 0.19.5 | Replace | The same version pin now supplies Fabric and the REI annotation-only compile dependency on Forge-family nodes. Artifact audit confirms Fabric Loader is not packaged. |
| Stonecutter (#9) | 0.9.7 | 0.9.8 | Replace | Evaluated on the current source/toolchain; all nine builds, tests, and Fabric GameTests passed. |
| Stonecraft (#14) | 1.10.14 | PR 1.14.3; newest observed 1.14.5 | Keep 1.10.14; defer newer release | PR 1.14.3 has a reproducible NeoForge 1.21.1 `NoSuchFileException: mainargs.txt`; it is obsolete as an update candidate. The 1.14.5 evaluation could not resolve its Loom 1.17 snapshot because `maven.architectury.dev` timed out. Do not substitute an unverified plugin update. |

The latest Stonecraft version observed on the Gradle Plugin Portal was 1.14.5 (published 2026-10-13). Its test is deferred, not declared incompatible. The requested repository-only changes do not modify `.github/dependabot.yml`; the standard public Maven endpoints do not need private-registry configuration.

## Transient download handling

`scripts/download_utils.py` retries only transient HTTP statuses (408, 425, 429, 500, 502, 503, 504) and transient connection/timeout/incomplete-read errors, at most three attempts with 1s/2s delays. Non-transient HTTP errors such as 404 fail immediately. Quilt's Java installer needs a separate bounded retry because it can exit zero after logging a metadata 502/503 without producing its launcher; the smoke checks the named `meta.quiltmc.org` response and the expected launcher file. These retries do not hide a loader startup failure, weaken integrity checks, or retry indefinitely. Seven Python tests cover the retry and non-retry paths.

## Dependabot PR disposition

These PRs were evaluated against the current starting tree; stale PR check results are not a substitute for that evaluation. The current branch workflows subsequently passed on the maintenance ref. The Dependabot update job itself was not rerun: GitHub's updater operates against the default branch, which this maintenance work intentionally does not modify.

| PR | Disposition | Follow-up |
| --- | --- | --- |
| #2 checkout v7 | Replace | Supersede with maintenance branch workflow changes. |
| #6 Fabric Loader 0.19.5 | Replace | Supersede with the coherent version-pin update. |
| #7 setup-java v6 | Replace | Supersede with maintenance branch workflow changes. |
| #9 Stonecutter 0.9.8 | Replace | Supersede with maintenance branch plugin update. |
| #14 Stonecraft 1.14.3 | Obsolete / defer | Close the outdated PR after branch integration; separately test current 1.14.5 when its Loom snapshot repository is reachable. |
| #15 download-artifact v7 | Replace | Supersede with maintenance branch workflow changes. |
| #16 upload-artifact v6 | Replace | Supersede with maintenance branch workflow changes. |
| #17 Gradle 9.8.1 | Replace | Supersede with generated wrapper update; the old Quilt failure was transient HTTP 503. |

No PR is merged into `main` as part of this branch. Close or supersede the listed Dependabot PRs only after reviewing/integrating the maintenance branch.
