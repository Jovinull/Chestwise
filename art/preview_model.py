#!/usr/bin/env python3
"""Render orthographic previews of a block model straight from its JSON.

This is a verification aid, not a shipped asset. It rasterises the front, side
and top views by sampling each element face through its declared UV window, so
a mis-aimed UV rectangle or a mis-sized element shows up here without launching
Minecraft.

    python art/preview_model.py
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "chestwise"
MODEL = ASSETS / "models" / "block" / "storage_terminal.json"
TEXTURES = ASSETS / "textures" / "block"

SCALE = 24
BACKDROP = (24, 24, 28, 255)


def load_textures(model: dict) -> dict[str, Image.Image]:
    loaded = {}
    for key, reference in model["textures"].items():
        name = reference.split(":")[-1].split("/")[-1]
        path = TEXTURES / f"{name}.png"
        if path.is_file():
            loaded[key] = Image.open(path).convert("RGBA")
    return loaded


def resolve(model: dict, textures: dict[str, Image.Image], reference: str) -> Image.Image | None:
    key = reference.lstrip("#")
    return textures.get(key)


def draw_face(
    canvas: Image.Image,
    texture: Image.Image,
    uv: list[float],
    left: float,
    top: float,
    width: float,
    height: float,
) -> None:
    """Paste the UV window of `texture` into the canvas rectangle, unfiltered."""
    u1, v1, u2, v2 = (int(round(value)) for value in uv)
    if u2 <= u1 or v2 <= v1:
        return
    patch = texture.crop((u1, v1, u2, v2))
    target = (int(round(width * SCALE)), int(round(height * SCALE)))
    if target[0] <= 0 or target[1] <= 0:
        return
    patch = patch.resize(target, Image.NEAREST)
    canvas.paste(patch, (int(round(left * SCALE)), int(round(top * SCALE))), patch)


def render_view(model: dict, textures: dict[str, Image.Image], view: str) -> Image.Image:
    canvas = Image.new("RGBA", (16 * SCALE, 16 * SCALE), BACKDROP)
    elements = model["elements"]

    if view == "front":
        # Looking from -Z. Nearest element (smallest z) is painted last.
        ordered = sorted(elements, key=lambda e: -e["from"][2])
        face = "north"
    elif view == "side":
        # Looking from -X.
        ordered = sorted(elements, key=lambda e: -e["from"][0])
        face = "west"
    else:
        # Looking down. Highest element painted last.
        ordered = sorted(elements, key=lambda e: e["to"][1])
        face = "up"

    for element in ordered:
        faces = element.get("faces", {})
        if face not in faces:
            continue
        definition = faces[face]
        texture = resolve(model, textures, definition["texture"])
        if texture is None:
            continue
        x1, y1, z1 = element["from"]
        x2, y2, z2 = element["to"]
        if view == "front":
            left, top, width, height = x1, 16 - y2, x2 - x1, y2 - y1
        elif view == "side":
            left, top, width, height = z1, 16 - y2, z2 - z1, y2 - y1
        else:
            left, top, width, height = x1, z1, x2 - x1, z2 - z1
        draw_face(canvas, texture, definition["uv"], left, top, width, height)
    return canvas


def main() -> int:
    model = json.loads(MODEL.read_text(encoding="utf-8"))
    textures = load_textures(model)
    views = ["front", "side", "top"]
    rendered = [render_view(model, textures, view) for view in views]

    gap = 12
    sheet = Image.new(
        "RGBA",
        (sum(image.width for image in rendered) + gap * (len(rendered) - 1), rendered[0].height),
        (0, 0, 0, 0),
    )
    offset = 0
    for image in rendered:
        sheet.paste(image, (offset, 0))
        offset += image.width + gap
    destination = ROOT / "art" / "_preview_model.png"
    sheet.save(destination)
    print(f"wrote {destination.relative_to(ROOT)} ({', '.join(views)})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
