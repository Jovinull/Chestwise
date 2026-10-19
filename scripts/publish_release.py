#!/usr/bin/env python3
"""Validate, or stage, the nine Chestwise release artifacts on mod platforms.

Uploads are deliberately staged: Modrinth versions are created as drafts and
CurseForge files are held for manual release. Promotion to public is a separate
human action after all files and hashes have been reviewed.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
import urllib.error
import urllib.request
import uuid
import zipfile
from dataclasses import dataclass
from pathlib import Path


FABRIC_API_PROJECT_ID = "P7dR8mSH"
MODRINTH_VERSION_URL = "https://api.modrinth.com/v2/version"
CURSEFORGE_UPLOAD_URL = "https://www.curseforge.com/api/projects/{project_id}/upload-file"


@dataclass(frozen=True)
class Artifact:
    minecraft: str
    loader: str
    filename: str

    @property
    def modrinth_version(self) -> str:
        return f"0.1.0+mc{self.minecraft}-{self.loader}"


ARTIFACTS = tuple(
    Artifact(mc, loader, f"chestwise-{loader}-{mc}-0.1.0.jar")
    for mc in ("1.20.1", "1.21.1", "26.3")
    for loader in ("fabric", "forge", "neoforge")
)


class ReleaseValidationError(Exception):
    """An input, mapping, or artifact validation failed."""


def _read_sums(path: Path) -> dict[str, str]:
    if not path.is_file():
        raise ReleaseValidationError(f"SHA256SUMS not found: {path}")

    result: dict[str, str] = {}
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        match = re.fullmatch(r"([0-9a-fA-F]{64})\s+\*?(.+)", line)
        if not match:
            raise ReleaseValidationError(f"Malformed SHA256SUMS line {line_number}")
        filename = Path(match.group(2)).name
        if filename in result:
            raise ReleaseValidationError(f"Duplicate checksum entry: {filename}")
        result[filename] = match.group(1).lower()
    return result


def _metadata_path(artifact: Artifact) -> str:
    if artifact.loader == "fabric":
        return "fabric.mod.json"
    if artifact.loader == "forge" or artifact.minecraft == "1.20.1":
        return "META-INF/mods.toml"
    return "META-INF/neoforge.mods.toml"


def _validate_jar_metadata(path: Path, artifact: Artifact, version: str) -> None:
    metadata_path = _metadata_path(artifact)
    try:
        with zipfile.ZipFile(path) as jar:
            raw = jar.read(metadata_path).decode("utf-8")
            manifest = (
                jar.read("META-INF/MANIFEST.MF").decode("utf-8")
                if "META-INF/MANIFEST.MF" in jar.namelist()
                else ""
            )
    except (OSError, KeyError, UnicodeError, zipfile.BadZipFile) as error:
        raise ReleaseValidationError(f"{artifact.filename}: cannot read {metadata_path}: {error}") from error

    if artifact.loader == "fabric":
        try:
            metadata = json.loads(raw)
        except json.JSONDecodeError as error:
            raise ReleaseValidationError(f"{artifact.filename}: invalid fabric.mod.json") from error
        expected_game_range = artifact.minecraft
        dependencies = metadata.get("depends", {})
        if metadata.get("id") != "chestwise" or metadata.get("version") != version:
            raise ReleaseValidationError(f"{artifact.filename}: incorrect Fabric mod id/version")
        if dependencies.get("minecraft") != expected_game_range or "fabric-api" not in dependencies:
            raise ReleaseValidationError(f"{artifact.filename}: incorrect Fabric Minecraft/API dependencies")
        if "fabric-gametest" in metadata.get("entrypoints", {}):
            raise ReleaseValidationError(f"{artifact.filename}: production GameTest entrypoint present")
        return

    if not re.search(r'(?m)^modId\s*=\s*"chestwise"\s*$', raw):
        raise ReleaseValidationError(f"{artifact.filename}: incorrect Forge-family mod id")
    version_match = re.search(r'(?m)^version\s*=\s*"([^"]+)"\s*$', raw)
    if version_match is None:
        raise ReleaseValidationError(f"{artifact.filename}: incorrect Forge-family mod version")
    metadata_version = version_match.group(1)
    if metadata_version != version and not (
        metadata_version == "${file.jarVersion}"
        and re.search(rf"(?m)^Implementation-Version:\s*{re.escape(version)}\s*$", manifest)
    ):
        raise ReleaseValidationError(f"{artifact.filename}: incorrect Forge-family mod version")

    minecraft_ranges = {
        ("1.20.1", "forge"): "[1.20.1]",
        ("1.20.1", "neoforge"): "[1.20.1]",
        ("1.21.1", "forge"): "[1.21.1]",
        ("1.21.1", "neoforge"): "[1.21.1]",
        ("26.3", "forge"): "[26.3,26.4)",
        ("26.3", "neoforge"): "[26.3]",
    }
    expected_minecraft_range = minecraft_ranges[(artifact.minecraft, artifact.loader)]
    dependency_blocks = re.finditer(
        r"(?ms)^\[\[dependencies\.chestwise\]\]\s*(.*?)(?=^\[|\Z)", raw
    )
    actual_minecraft_ranges = [
        match.group(1)
        for block in dependency_blocks
        if re.search(r'(?m)^modId\s*=\s*"minecraft"\s*$', block.group(1))
        for match in [re.search(r'(?m)^versionRange\s*=\s*"([^"]+)"\s*$', block.group(1))]
        if match is not None
    ]
    if expected_minecraft_range not in actual_minecraft_ranges:
        raise ReleaseValidationError(f"{artifact.filename}: incorrect Minecraft version range")


def validate_package(artifact_dir: Path, version: str) -> list[tuple[Artifact, Path, str]]:
    if version != "0.1.0":
        raise ReleaseValidationError("This publishing preparer is scoped to Chestwise 0.1.0")
    if not artifact_dir.is_dir():
        raise ReleaseValidationError(f"Artifact directory not found: {artifact_dir}")

    expected_names = {artifact.filename for artifact in ARTIFACTS}
    actual_names = {path.name for path in artifact_dir.glob("*.jar")}
    if actual_names != expected_names:
        missing = sorted(expected_names - actual_names)
        extra = sorted(actual_names - expected_names)
        raise ReleaseValidationError(f"Expected exactly nine release JARs; missing={missing}, extra={extra}")

    checksums = _read_sums(artifact_dir / "SHA256SUMS")
    if set(checksums) != expected_names:
        raise ReleaseValidationError("SHA256SUMS must contain exactly the nine release JARs")

    validated: list[tuple[Artifact, Path, str]] = []
    for artifact in ARTIFACTS:
        path = artifact_dir / artifact.filename
        sha256 = hashlib.sha256(path.read_bytes()).hexdigest()
        if sha256 != checksums[artifact.filename]:
            raise ReleaseValidationError(f"SHA-256 mismatch: {artifact.filename}")
        _validate_jar_metadata(path, artifact, version)
        validated.append((artifact, path, sha256))
    return validated


def _modrinth_payload(artifact: Artifact, project_id: str) -> dict[str, object]:
    dependencies: list[dict[str, str]] = []
    if artifact.loader == "fabric":
        dependencies.append({"project_id": FABRIC_API_PROJECT_ID, "dependency_type": "required"})
    return {
        "name": f"Chestwise 0.1.0 for Minecraft {artifact.minecraft} {artifact.loader.title()}",
        "version_number": artifact.modrinth_version,
        "changelog": "First public beta of Chestwise 0.1.0.",
        "dependencies": dependencies,
        "game_versions": [artifact.minecraft],
        "version_type": "beta",
        "loaders": [artifact.loader],
        "status": "draft",
        "requested_status": "draft",
        "project_id": project_id,
        "file_parts": ["file"],
        "primary_file": "file",
        "environment": "client_and_server",
    }


def _curseforge_payload(artifact: Artifact) -> dict[str, object]:
    relations: dict[str, object] = {"projects": []}
    if artifact.loader == "fabric":
        relations["projects"] = [
            {"slug": "fabric-api", "type": ["requiredDependency"]}
        ]
    return {
        "changelog": "First public beta of Chestwise 0.1.0.",
        "changelogType": "markdown",
        "displayName": f"Chestwise 0.1.0 — Minecraft {artifact.minecraft} {artifact.loader.title()}",
        "gameVersionNames": [artifact.minecraft, artifact.loader.title()],
        "releaseType": "beta",
        "isMarkedForManualRelease": True,
        "relations": relations,
    }


def _multipart(fields: dict[str, str], file_name: str, file_path: Path) -> tuple[bytes, str]:
    boundary = f"----Chestwise{uuid.uuid4().hex}"
    chunks: list[bytes] = []
    for name, value in fields.items():
        chunks.extend(
            [
                f"--{boundary}\r\n".encode(),
                f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode(),
                value.encode("utf-8"),
                b"\r\n",
            ]
        )
    chunks.extend(
        [
            f"--{boundary}\r\n".encode(),
            f'Content-Disposition: form-data; name="file"; filename="{file_name}"\r\n'.encode(),
            b"Content-Type: application/java-archive\r\n\r\n",
            file_path.read_bytes(),
            b"\r\n",
            f"--{boundary}--\r\n".encode(),
        ]
    )
    return b"".join(chunks), f"multipart/form-data; boundary={boundary}"


def _post(url: str, headers: dict[str, str], body: bytes) -> str:
    request = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            if not 200 <= response.status < 300:
                raise ReleaseValidationError(f"Upload returned HTTP {response.status}")
            return response.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as error:
        raise ReleaseValidationError(f"Upload failed with HTTP {error.code}") from error
    except urllib.error.URLError as error:
        raise ReleaseValidationError(f"Upload failed due to a network error: {error.reason}") from error


def _require_publish_configuration(version: str) -> dict[str, str]:
    tag = os.environ.get("GITHUB_REF", "")
    event_name = os.environ.get("GITHUB_EVENT_NAME", "")
    if event_name != "push" or tag != f"refs/tags/v{version}":
        raise ReleaseValidationError("Publishing is permitted only on a push of the exact v0.1.0 tag")
    names = (
        "CHESTWISE_MODRINTH_PROJECT_ID",
        "CHESTWISE_CURSEFORGE_PROJECT_ID",
        "MODRINTH_TOKEN",
        "CURSEFORGE_TOKEN",
    )
    missing = [name for name in names if not os.environ.get(name)]
    if missing:
        raise ReleaseValidationError(
            "Publishing configuration is incomplete; no upload was attempted. Missing: "
            + ", ".join(missing)
        )
    return {name: os.environ[name] for name in names}


def _stage_uploads(
    artifacts: list[tuple[Artifact, Path, str]], configuration: dict[str, str]
) -> None:
    modrinth_project_id = configuration["CHESTWISE_MODRINTH_PROJECT_ID"]
    curseforge_project_id = configuration["CHESTWISE_CURSEFORGE_PROJECT_ID"]
    for artifact, path, sha256 in artifacts:
        payload = _modrinth_payload(artifact, modrinth_project_id)
        body, content_type = _multipart({"data": json.dumps(payload)}, path.name, path)
        response = _post(
            MODRINTH_VERSION_URL,
            {"Authorization": configuration["MODRINTH_TOKEN"], "Content-Type": content_type},
            body,
        )
        print(f"Modrinth draft staged: {artifact.filename} sha256={sha256} response={response[:160]}")

        payload = _curseforge_payload(artifact)
        body, content_type = _multipart({"metadata": json.dumps(payload)}, path.name, path)
        response = _post(
            CURSEFORGE_UPLOAD_URL.format(project_id=curseforge_project_id),
            {"X-Api-Token": configuration["CURSEFORGE_TOKEN"], "Content-Type": content_type},
            body,
        )
        print(f"CurseForge manual-release file staged: {artifact.filename} sha256={sha256} response={response[:160]}")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--artifact-dir", type=Path, default=Path("build/release"))
    parser.add_argument("--version", default="0.1.0")
    parser.add_argument("--publish", action="store_true", help="stage private/draft uploads; tag-only")
    args = parser.parse_args(argv)

    try:
        artifacts = validate_package(args.artifact_dir, args.version)
        if not args.publish:
            print(f"Release package dry-run PASS: {len(artifacts)} artifacts, 18 staged platform uploads")
            for artifact, _path, sha256 in artifacts:
                print(
                    f"{artifact.filename}: Modrinth {artifact.modrinth_version} "
                    f"[{artifact.minecraft}/{artifact.loader}], CurseForge "
                    f"[{artifact.minecraft}/{artifact.loader}], sha256={sha256}"
                )
            return 0

        configuration = _require_publish_configuration(args.version)
        _stage_uploads(artifacts, configuration)
        print("All 18 platform files are staged as Modrinth drafts/CurseForge manual-release files.")
        return 0
    except ReleaseValidationError as error:
        print(f"Release publishing validation failed: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
