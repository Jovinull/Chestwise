#!/usr/bin/env python3
"""Derive the Blockbench source file from the shipped block model.

The block model JSON is the single source of truth. This script mirrors it into
`art/blockbench/storage_terminal.bbmodel` with the textures embedded, so the
Blockbench file can never silently drift from what Minecraft actually loads.

    python art/build_bbmodel.py
"""

from __future__ import annotations

import base64
import json
import uuid
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "chestwise"
MODEL = ASSETS / "models" / "block" / "storage_terminal.json"
TEXTURES = ASSETS / "textures" / "block"
DESTINATION = ROOT / "art" / "blockbench" / "storage_terminal.bbmodel"

# Deterministic namespace so re-running the script does not churn the file.
NAMESPACE = uuid.UUID("6d0b6f6c-3d1a-4f77-9a2a-8f2d5c1c9b40")


def stable_uuid(label: str) -> str:
    return str(uuid.uuid5(NAMESPACE, label))


def embed(path: Path) -> str:
    return "data:image/png;base64," + base64.b64encode(path.read_bytes()).decode("ascii")


def main() -> int:
    model = json.loads(MODEL.read_text(encoding="utf-8"))

    # Blockbench addresses textures by index, so fix an order and map the
    # model's "#name" references onto it.
    texture_keys = [key for key in model["textures"] if key != "particle"]
    index_of = {key: position for position, key in enumerate(texture_keys)}
    particle_key = model["textures"]["particle"].split("/")[-1]

    textures = []
    for position, key in enumerate(texture_keys):
        name = model["textures"][key].split("/")[-1]
        source = TEXTURES / f"{name}.png"
        if not source.is_file():
            raise FileNotFoundError(source)
        textures.append({
            "path": "",
            "name": f"{name}.png",
            "folder": "block",
            "namespace": "chestwise",
            "id": str(position),
            "particle": name == particle_key,
            "render_mode": "normal",
            "visible": True,
            "mode": "bitmap",
            "saved": False,
            "uuid": stable_uuid(f"texture:{name}"),
            "source": embed(source),
        })

    elements = []
    outliner = []
    for position, element in enumerate(model["elements"]):
        name = element.get("__name") or f"element_{position}"
        element_uuid = stable_uuid(f"element:{position}")
        faces = {}
        for face in ("north", "east", "south", "west", "up", "down"):
            definition = element.get("faces", {}).get(face)
            if definition is None:
                # Blockbench expects every face key; an empty texture hides it,
                # which is exactly how the shipped model omits coplanar faces.
                faces[face] = {"uv": [0, 0, 0, 0], "texture": None}
                continue
            faces[face] = {
                "uv": definition["uv"],
                "texture": index_of[definition["texture"].lstrip("#")],
            }
        elements.append({
            "name": name,
            "box_uv": False,
            "rescale": False,
            "locked": False,
            "render_order": "default",
            "allow_mirror_modeling": True,
            "from": element["from"],
            "to": element["to"],
            "autouv": 0,
            "color": position % 8,
            "origin": [8, 0, 8],
            "faces": faces,
            "type": "cube",
            "uuid": element_uuid,
        })
        outliner.append(element_uuid)

    bbmodel = {
        "meta": {
            "format_version": "4.10",
            "model_format": "java_block",
            "box_uv": False,
        },
        "name": "storage_terminal",
        "parent": model.get("parent", ""),
        "ambientocclusion": True,
        "front_gui_light": False,
        "visible_box": [1, 1, 0],
        "variable_placeholders": "",
        "variable_placeholder_buttons": [],
        "timeline_setups": [],
        "unhandled_root_fields": {},
        "resolution": {"width": 16, "height": 16},
        "elements": elements,
        "outliner": outliner,
        "textures": textures,
    }

    DESTINATION.parent.mkdir(parents=True, exist_ok=True)
    DESTINATION.write_text(json.dumps(bbmodel, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {DESTINATION.relative_to(ROOT)} ({len(elements)} elements, {len(textures)} textures)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
