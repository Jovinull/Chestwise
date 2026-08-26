#!/usr/bin/env python3
"""Validate and assemble the nine supported Chestwise release artifacts."""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import struct
import sys
import zipfile
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


@dataclass(frozen=True)
class Artifact:
    minecraft: str
    loader: str
    relative_path: str
    metadata: str

    @property
    def path(self) -> Path:
        return ROOT / self.relative_path


ARTIFACTS = (
    Artifact("1.20.1", "fabric", "versions/1.20.1-fabric/build/libs/chestwise-fabric-1.20.1-0.1.0.jar", "fabric.mod.json"),
    Artifact("1.20.1", "forge", "versions/1.20.1-forge/build/libs/chestwise-forge-1.20.1-0.1.0.jar", "META-INF/mods.toml"),
    Artifact("1.20.1", "neoforge", "platforms/legacy-neoforge-1.20.1/build/libs/chestwise-neoforge-1.20.1-0.1.0.jar", "META-INF/mods.toml"),
    Artifact("1.21.1", "fabric", "versions/1.21.1-fabric/build/libs/chestwise-fabric-1.21.1-0.1.0.jar", "fabric.mod.json"),
    Artifact("1.21.1", "forge", "versions/1.21.1-forge/build/libs/chestwise-forge-1.21.1-0.1.0.jar", "META-INF/mods.toml"),
    Artifact("1.21.1", "neoforge", "versions/1.21.1-neoforge/build/libs/chestwise-neoforge-1.21.1-0.1.0.jar", "META-INF/neoforge.mods.toml"),
    Artifact("26.2", "fabric", "versions/26.2-fabric/build/libs/chestwise-fabric-26.2-0.1.0.jar", "fabric.mod.json"),
    Artifact("26.2", "forge", "platforms/forge-26.2/build/libs/chestwise-forge-26.2-0.1.0.jar", "META-INF/mods.toml"),
    Artifact("26.2", "neoforge", "versions/26.2-neoforge/build/libs/chestwise-neoforge-26.2-0.1.0.jar", "META-INF/neoforge.mods.toml"),
)

COMMON_ENTRIES = {
    "assets/chestwise/icon.png",
    "assets/chestwise/lang/en_us.json",
    "assets/chestwise/lang/pt_br.json",
    "assets/chestwise/blockstates/storage_terminal.json",
    "assets/chestwise/models/block/storage_terminal.json",
    "assets/chestwise/models/item/storage_terminal.json",
    "dev/chestwise/minecraft/StorageTerminalBlockEntity.class",
    "pack.mcmeta",
}


def fail(message: str) -> None:
    raise ValueError(message)


def validate(artifact: Artifact) -> str:
    path = artifact.path
    if not path.is_file():
        fail(f"missing artifact: {path.relative_to(ROOT)}")
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        entries = set(names)
        if len(entries) != len(names):
            fail(f"{path.name}: duplicate ZIP entries")
        missing = COMMON_ENTRIES - entries
        if missing:
            fail(f"{path.name}: missing {sorted(missing)}")
        if artifact.metadata not in entries:
            fail(f"{path.name}: missing {artifact.metadata}")
        wrong_metadata = {"fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml"} - {artifact.metadata}
        present_wrong = wrong_metadata & entries
        if present_wrong:
            fail(f"{path.name}: cross-loader metadata {sorted(present_wrong)}")

        modern = artifact.minecraft != "1.20.1"
        recipe = f"data/chestwise/{'recipe' if modern else 'recipes'}/storage_terminal.json"
        loot = f"data/chestwise/{'loot_table' if modern else 'loot_tables'}/blocks/storage_terminal.json"
        if recipe not in entries or loot not in entries:
            fail(f"{path.name}: missing version-correct data paths")
        wrong_recipe = f"data/chestwise/{'recipes' if modern else 'recipe'}/storage_terminal.json"
        wrong_loot = f"data/chestwise/{'loot_tables' if modern else 'loot_table'}/blocks/storage_terminal.json"
        if wrong_recipe in entries or wrong_loot in entries:
            fail(f"{path.name}: retained data paths for another Minecraft generation")

        icon = archive.read("assets/chestwise/icon.png")
        if icon[:8] != b"\x89PNG\r\n\x1a\n" or len(icon) < 24:
            fail(f"{path.name}: icon is not a valid PNG")
        width, height = struct.unpack(">II", icon[16:24])
        if (width, height) != (256, 256):
            fail(f"{path.name}: icon is {width}x{height}, expected 256x256")

        metadata_text = archive.read(artifact.metadata).decode("utf-8")
        if artifact.loader == "fabric":
            metadata = json.loads(metadata_text)
            if metadata.get("id") != "chestwise" or metadata.get("icon") != "assets/chestwise/icon.png":
                fail(f"{path.name}: invalid Fabric identity or icon")
            if metadata.get("depends", {}).get("minecraft") != artifact.minecraft:
                fail(f"{path.name}: invalid Fabric Minecraft constraint")
        elif "modId = \"chestwise\"" not in metadata_text and 'modId="chestwise"' not in metadata_text:
            fail(f"{path.name}: invalid Forge-family mod id")
        elif 'logoFile = "assets/chestwise/icon.png"' not in metadata_text and 'logoFile="assets/chestwise/icon.png"' not in metadata_text:
            fail(f"{path.name}: missing Forge-family icon declaration")

    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check-only", action="store_true", help="validate without assembling build/release")
    args = parser.parse_args()
    checksums: list[tuple[str, str]] = []
    for artifact in ARTIFACTS:
        digest = validate(artifact)
        checksums.append((digest, artifact.path.name))
        print(f"PASS {artifact.loader:8} {artifact.minecraft:6} {artifact.path.name}")

    if not args.check_only:
        destination = ROOT / "build" / "release"
        destination.mkdir(parents=True, exist_ok=True)
        for artifact in ARTIFACTS:
            shutil.copy2(artifact.path, destination / artifact.path.name)
        checksum_file = destination / "SHA256SUMS"
        checksum_file.write_text(
            "".join(f"{digest}  {name}\n" for digest, name in checksums),
            encoding="utf-8",
            newline="\n",
        )
        print(f"Assembled {len(ARTIFACTS)} artifacts in {destination.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, zipfile.BadZipFile, json.JSONDecodeError) as error:
        print(f"FAIL {error}", file=sys.stderr)
        sys.exit(1)
