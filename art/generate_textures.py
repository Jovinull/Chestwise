#!/usr/bin/env python3
"""Author the Storage Terminal textures as explicit pixel maps.

Every pixel is placed by hand through the character grids below; nothing is
procedural, filtered, or resampled. Run from the repository root:

    python art/generate_textures.py

Textures are written straight into the shared resource tree, so all loaders and
all Minecraft versions consume the same art.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src" / "main" / "resources" / "assets" / "chestwise" / "textures" / "block"


# Chestwise palette. Wood is the structure, copper marks the pulls and corner
# brackets, iron marks structural straps, paper marks anything catalogued.
PALETTE = {
    "1": (46, 32, 25),      # wood, deepest shadow / seam
    "2": (61, 43, 32),      # wood, dark
    "3": (78, 56, 40),      # wood, mid (base tone)
    "4": (97, 70, 49),      # wood, light
    "5": (116, 85, 57),     # wood, highlight
    "c": (107, 58, 36),     # copper, shadow
    "C": (166, 90, 52),     # copper, mid
    "H": (200, 122, 74),    # copper, highlight
    "i": (90, 90, 96),      # iron, shadow
    "I": (126, 126, 134),   # iron, mid
    "L": (168, 168, 176),   # iron, highlight
    "p": (181, 166, 131),   # paper, shadow
    "P": (216, 203, 168),   # paper, face
    "m": (138, 124, 94),    # paper, written mark
}


# --------------------------------------------------------------------------
# Front plane: catalogue card slots (rows 1-4), worktop lip (rows 5-6) and the
# drawer stack (rows 7-15). Row 0 sits above the shelf and is never sampled.
# --------------------------------------------------------------------------
FRONT = [
    "3333333333333333",
    "2222222222222222",
    "21PP131PP131PP12",
    "21Pm131Pm131Pm12",
    "21CH131CH131CH12",
    "5555555555555555",
    "3CC2222222222CC3",
    "2444444444444442",
    "23PPPPPCHC333332",
    "2111111111111112",
    "2444444444444442",
    "2333333CHC333332",
    "2111111111111112",
    "2444444444444442",
    "2333333CHC333332",
    "2111111111111112",
]

# --------------------------------------------------------------------------
# Side plane. Kept to vertical straps and horizontal grain so the art still
# reads correctly when the east face samples it mirrored.
# --------------------------------------------------------------------------
SIDE = [
    "3333333333333333",
    "3333333332222222",
    "3333333333333332",
    "333333333I333332",
    "333333333II22222",
    "5555555555555555",
    "3CC2222222222CC3",
    "2444444444444442",
    "233I333333333332",
    "233I333333333332",
    "244I444444444442",
    "233I333333333332",
    "233I333333333332",
    "244I444444444442",
    "233I333333333332",
    "2111111111111112",
]

# --------------------------------------------------------------------------
# Back plane: plain panelling braced with iron. No pulls, no labels, no cables.
# --------------------------------------------------------------------------
BACK = [
    "3333333333333333",
    "2222222222222222",
    "3333333333333333",
    "33IIIIIIIIIIII33",
    "2222222222222222",
    "5555555555555555",
    "3CC2222222222CC3",
    "2444444444444442",
    "2333333333333332",
    "2333333333333332",
    "2344444444444432",
    "2333333333333332",
    "23IIIIIIIIIIII32",
    "2333333333333332",
    "2344444444444432",
    "2111111111111112",
]

# --------------------------------------------------------------------------
# Worktop surface. Row 0 is the front lip (z=0) and carries the copper corner
# brackets; rows 9-15 sit under the shelf and are never seen.
# --------------------------------------------------------------------------
TOP = [
    "2CC2222222222CC2",
    "3333333333333333",
    "3444444444444443",
    "3333333333333333",
    "2222222222222222",
    "3333333333333333",
    "3444444444444443",
    "3333333333333333",
    "2222222222222222",
    "3333333333333333",
    "3444444444444443",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
]

# --------------------------------------------------------------------------
# Shelf top. Rows 9-15 are the only sampled band; an iron lip fronts the shelf.
# --------------------------------------------------------------------------
CATALOGUE_TOP = [
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "2IIIIIIIIIIIIII2",
    "2333333333333332",
    "2344444444444432",
    "2333333333333332",
    "2333333333333332",
    "2344444444444432",
    "2222222222222222",
]

# --------------------------------------------------------------------------
# Open catalogue lying on the desk: two written pages, dark spine, copper
# bookmark. Sampled at rows 3-7, columns 3-12. It stops one pixel short of the
# shelf so its rear face never lands coplanar with the catalogue front.
# --------------------------------------------------------------------------
BOOK = [
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "333PPPP11PPPP333",
    "333PmmP11PmmP333",
    "333PPPP11PPPP333",
    "333PmmPC1PmmP333",
    "333pppp11pppp333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
    "3333333333333333",
]


TEXTURES = {
    "storage_terminal_front": FRONT,
    "storage_terminal_side": SIDE,
    "storage_terminal_back": BACK,
    "storage_terminal_top": TOP,
    "storage_terminal_catalogue_top": CATALOGUE_TOP,
    "storage_terminal_book": BOOK,
}


def render(rows: list[str]) -> Image.Image:
    if len(rows) != 16:
        raise ValueError(f"expected 16 rows, got {len(rows)}")
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y, row in enumerate(rows):
        if len(row) != 16:
            raise ValueError(f"row {y} has {len(row)} columns, expected 16")
        for x, symbol in enumerate(row):
            if symbol not in PALETTE:
                raise ValueError(f"row {y} column {x}: unknown palette symbol {symbol!r}")
            pixels[x, y] = (*PALETTE[symbol], 255)
    return image


def main() -> int:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name, rows in TEXTURES.items():
        destination = OUTPUT / f"{name}.png"
        render(rows).save(destination)
        print(f"wrote {destination.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
