# Chestwise 0.1.0 publishing preparation

Status: **staging workflow prepared; projects/credentials are not configured,
and publishing is not authorized**. Do not create a tag or publish platform
files as part of the release-candidate work.

## Artifact mapping

The release build produces exactly one JAR for each Minecraft/loader pair:

| Minecraft | Fabric | Forge | NeoForge |
| --- | --- | --- | --- |
| 1.20.1 | `chestwise-fabric-1.20.1-0.1.0.jar` | `chestwise-forge-1.20.1-0.1.0.jar` | `chestwise-neoforge-1.20.1-0.1.0.jar` |
| 1.21.1 | `chestwise-fabric-1.21.1-0.1.0.jar` | `chestwise-forge-1.21.1-0.1.0.jar` | `chestwise-neoforge-1.21.1-0.1.0.jar` |
| 26.3 | `chestwise-fabric-26.3-0.1.0.jar` | `chestwise-forge-26.3-0.1.0.jar` | `chestwise-neoforge-26.3-0.1.0.jar` |

Quilt uses the corresponding Fabric JAR; do not upload duplicate Quilt files.
Fabric API is a required dependency for Fabric/Quilt files only. Forge and
NeoForge files have no runtime library dependency.

Each upload must be matched to one exact Minecraft version and loader. Do not
upload all nine files as one platform version with a shared loader/version
label. Mark the first public `0.1.0` files as **Beta** on both platforms; retain
`0.1.0` in the artifact names and metadata.

## Chosen publication interface

Prefer the platforms' official APIs over a third-party publishing action that
would receive upload credentials:

- Modrinth creates each project version with `POST /version`. Each request
  carries its own game version, loader, beta status, required Fabric API
  dependency where applicable, and one JAR. The upload token needs the
  `VERSION_CREATE` scope. See the
  [official create-version API](https://docs.modrinth.com/api/operations/createversion/).
- CurseForge uploads each file with `POST
  /api/projects/{projectId}/upload-file`, using an `X-Api-Token` header and
  per-file game-version/loader metadata. The Fabric API relation is required
  only for Fabric files. See the
  [official CurseForge upload API](https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-api?locale=en).
- GitHub Release remains the CI-produced source of truth. Upload the exact same
  CI artifacts to the other platforms and compare their SHA-256 values against
  that run's `SHA256SUMS` before listing the files.

The current repository has no Modrinth or CurseForge project ID configured and
no platform publishing secrets configured. Before release, the owner must
reserve/create both platform projects and set repository variables
`CHESTWISE_MODRINTH_PROJECT_ID` and `CHESTWISE_CURSEFORGE_PROJECT_ID`. The owner
must also add repository secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`; do not
put credentials in metadata or commit history.

The platform staging job in `release.yml` is tag-only and runs before the
GitHub Release job. The publisher validates all nine paths, metadata, and
SHA-256 entries, then checks the exact `v0.1.0` tag, both project IDs, and both
tokens before its first network request. A `workflow_dispatch` on any ref,
including `refs/tags/v0.1.0`, packages and smoke-tests artifacts but skips
platform staging and GitHub publication. Only a tag `push` starts publishing.
Keep both conditions. If a platform API fails partway through,
already-uploaded files remain drafts/manual-release files; inspect the platform
before retrying to avoid duplicate drafts.

## Dry-run boundary

The package workflow, `scripts/audit_artifacts.py`, and
`scripts/publish_release.py` validate the nine artifact paths, embedded loader
metadata, Fabric API dependency mapping, platform loader/game-version mapping,
and `SHA256SUMS` without making network requests. That offline dry-run cannot
verify platform project permissions, accepted project IDs, or live category
metadata. Those remain unverified until the owner creates both projects,
configures IDs/tokens, and tests a tag-staged upload.

## Release procedure to prepare

1. Create/review both public project pages, reserve the `chestwise` slug where
   available, and set the beta release channel.
2. Add the two platform tokens as GitHub repository secrets and the project IDs
   as non-secret repository variables.
3. Run the package workflow's offline validation; test the live staging job on
   a disposable platform project before the public project is used.
4. After release approval only, create annotated tag `v0.1.0` on the exact
   reviewed `main` commit. No tag-signing setup has been verified; until signed
   tags are configured, use an annotated tag and verify its target SHA before
   pushing it.
5. Let tag CI build, test, audit, stage Modrinth drafts and CurseForge
   manual-release files, then publish the GitHub beta release. Review and
   promote the staged platform files manually only after verifying their nine
   JAR hashes against the CI manifest.
