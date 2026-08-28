#!/usr/bin/env python3
"""Draft the Chestwise mod icon and the terminal's GUI iconography.

The sheet is copied to `assets/chestwise/textures/gui/terminal_icons.png` and
indexed left to right. Only the glyphs with a control behind them are drawn; the
terminal has no restock button and locating is a middle-click, so those two stay
here until the actions exist. The mod icon is written straight to
`assets/chestwise/icon.png`.

    python art/generate_icons.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "chestwise"
GUI_SHEET = ASSETS / "textures" / "gui" / "terminal_icons.png"
MOD_ICON = ASSETS / "icon.png"

# Shared with the block textures so the family reads as one mod.
WOOD_DEEP = (46, 32, 25)
WOOD_DARK = (61, 43, 32)
WOOD_MID = (78, 56, 40)
WOOD_LIGHT = (97, 70, 49)
COPPER_MID = (166, 90, 52)
COPPER_LIGHT = (200, 122, 74)
IRON_MID = (126, 126, 134)
PAPER = (216, 203, 168)
PAPER_SHADOW = (181, 166, 131)
PAPER_MARK = (138, 124, 94)

ICON_PALETTE = {
    ".": None,
    "I": IRON_MID,
    "C": COPPER_MID,
    "H": COPPER_LIGHT,
    "P": PAPER,
    "m": PAPER_MARK,
}


# --------------------------------------------------------------------------
# GUI icons, 16x16 each. Every glyph has to survive at its native size, so the
# shapes stay to single-pixel strokes and solid blocks.
# --------------------------------------------------------------------------
SEARCH = [
    "................",
    ".....IIII.......",
    "...II....II.....",
    "..I........I....",
    "..I........I....",
    "..I........I....",
    "..I........I....",
    "...II....II.....",
    ".....IIII.......",
    "........CC......",
    ".........CC.....",
    "..........CC....",
    "...........CC...",
    "................",
    "................",
    "................",
]

DEPOSIT = [
    "................",
    "................",
    ".......CC.......",
    ".......CC.......",
    ".......CC.......",
    "....CCCCCCCC....",
    ".....CCCCCC.....",
    "......CCCC......",
    ".......CC.......",
    "................",
    "..I..........I..",
    "..I..........I..",
    "..I..........I..",
    "..IIIIIIIIIIII..",
    "................",
    "................",
]

QUICK_STACK = [
    "................",
    "................",
    "................",
    "................",
    "..PPPPP.........",
    "..PPPPP.........",
    "............C...",
    "..PPPPP......CC.",
    "..PPPPP..CCCCCC.",
    ".............CC.",
    "..PPPPP.....C...",
    "..PPPPP.........",
    "................",
    "................",
    "................",
    "................",
]

RESTOCK = [
    "................",
    "................",
    ".......CC.......",
    "......CCCC......",
    ".....CCCCCC.....",
    "....CCCCCCCC....",
    ".......CC.......",
    ".......CC.......",
    "................",
    "..IIIIIIIIIIII..",
    "..I..........I..",
    "..I..........I..",
    "..I..........I..",
    "..IIIIIIIIIIII..",
    "................",
    "................",
]

LOCATE = [
    "................",
    "................",
    ".....IIIII......",
    "....I.....I.....",
    "...I..CCC..I....",
    "...I..CCC..I....",
    "...I..CCC..I....",
    "....I.....I.....",
    ".....I...I......",
    "......I.I.......",
    ".......I........",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GUI_ICONS = [
    ("search", SEARCH),
    ("deposit", DEPOSIT),
    ("quick_stack", QUICK_STACK),
    ("restock", RESTOCK),
    ("locate", LOCATE),
]


def render_glyph(rows: list[str]) -> Image.Image:
    if len(rows) != 16:
        raise ValueError(f"expected 16 rows, got {len(rows)}")
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pixels = image.load()
    for y, row in enumerate(rows):
        if len(row) != 16:
            raise ValueError(f"row {y} has {len(row)} columns, expected 16")
        for x, symbol in enumerate(row):
            if symbol not in ICON_PALETTE:
                raise ValueError(f"row {y} column {x}: unknown symbol {symbol!r}")
            colour = ICON_PALETTE[symbol]
            if colour is not None:
                pixels[x, y] = (*colour, 255)
    return image


def build_gui_sheet() -> Image.Image:
    sheet = Image.new("RGBA", (16 * len(GUI_ICONS), 16), (0, 0, 0, 0))
    for index, (_, rows) in enumerate(GUI_ICONS):
        sheet.paste(render_glyph(rows), (index * 16, 0))
    return sheet


def build_mod_icon() -> Image.Image:
    """A single catalogue card lifted out of a drawer, not the whole block.

    Reduced to two objects so the silhouette still reads at 32 px.
    """
    icon = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(icon)

    # Backdrop: dark wood field with a deeper border, so the icon holds its
    # edge against both light and dark store pages.
    draw.rectangle([0, 0, 31, 31], fill=WOOD_DARK)
    draw.rectangle([0, 0, 31, 31], outline=WOOD_DEEP)
    draw.rectangle([1, 1, 30, 30], outline=WOOD_MID)

    # Drawer body, lower two thirds.
    draw.rectangle([4, 16, 27, 28], fill=WOOD_MID)
    draw.rectangle([4, 16, 27, 16], fill=WOOD_LIGHT)   # top bevel
    draw.rectangle([4, 28, 27, 28], fill=WOOD_DEEP)    # shadow under the drawer
    draw.rectangle([4, 16, 4, 28], fill=WOOD_LIGHT)
    draw.rectangle([27, 16, 27, 28], fill=WOOD_DEEP)

    # Copper pull, centred on the drawer face. Kept wide so it still reads as a
    # handle at 32 px rather than dissolving into a stray mark.
    draw.rectangle([11, 22, 20, 24], fill=COPPER_MID)
    draw.rectangle([11, 22, 20, 22], fill=COPPER_LIGHT)

    # Catalogue card rising out of the drawer, overlapping its top edge so the
    # two objects read as one action rather than two stacked shapes.
    draw.rectangle([9, 5, 22, 18], fill=PAPER)
    draw.rectangle([9, 5, 22, 5], fill=PAPER_SHADOW)
    draw.rectangle([9, 17, 22, 18], fill=PAPER_SHADOW)
    draw.rectangle([9, 5, 9, 18], fill=PAPER_SHADOW)

    # Three written lines: the card is an index, not a blank sheet.
    for row in (9, 12, 15):
        draw.rectangle([12, row, 19, row], fill=PAPER_MARK)

    return icon


def main() -> int:
    GUI_SHEET.parent.mkdir(parents=True, exist_ok=True)
    sheet = build_gui_sheet()
    sheet.save(GUI_SHEET)
    sheet.resize((sheet.width * 8, sheet.height * 8), Image.NEAREST).save(ROOT / "art" / "_preview_gui_icons.png")
    print(f"wrote {GUI_SHEET.relative_to(ROOT)} ({', '.join(name for name, _ in GUI_ICONS)})")

    # 32 px is the drawing size; the shipped icon is a nearest-neighbour upscale
    # so the pixels stay hard-edged at the 256 px the launchers expect.
    icon = build_mod_icon()
    icon.resize((256, 256), Image.NEAREST).save(MOD_ICON)
    print(f"wrote {MOD_ICON.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
