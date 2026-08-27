# Chestwise art direction

The rule every asset answers to: **your chests, one interface.** Chestwise does
not store anything digitally, so nothing may look like a computer. If a viewer
reads a block as a screen, a server, a disk drive, or a powered machine, the
asset is wrong regardless of how well it is drawn.

## Visual identity

**Vanilla Mechanical Cataloguer.** A cabinetmaker's answer to a storage room:
wood carcass, drawers, paper index cards, small copper and iron fittings. It
should look like something a villager could have built, and like a block that
could plausibly have shipped with the game.

Two questions gate any new asset:

1. Can a stranger infer "this organises or looks things up" from a screenshot?
2. Would a Minecraft player believe it came from a Vanilla+ mod?

## Palette

Reused verbatim across blocks, GUI icons, and the mod icon.

| Role | Hex | Use |
| --- | --- | --- |
| Wood deepest | `#2E2019` | Seams, drawer shadows, cast shadow |
| Wood dark | `#3D2B20` | Rails, recesses, backing panels |
| Wood mid | `#4E3828` | Base carcass tone |
| Wood light | `#614631` | Top bevels, grain |
| Wood highlight | `#745539` | Lit edges, work surface |
| Copper shadow | `#6B3A24` | Under-side of pulls |
| Copper mid | `#A65A34` | Drawer pulls, corner brackets |
| Copper highlight | `#C87A4A` | One-pixel catch light on pulls |
| Iron shadow | `#5A5A60` | Deep fittings |
| Iron mid | `#7E7E86` | Straps, braces, shelf lip |
| Iron highlight | `#A8A8B0` | Reserved; use sparingly |
| Paper | `#D8CBA8` | Index cards, labels, book pages |
| Paper shadow | `#B5A683` | Card edges, page underside |
| Paper mark | `#8A7C5E` | Written lines on a card |

Banned outright: neon blue, cyan, energetic purple, neon green, glowing black
panels, and any emissive texture. Nothing may read as powered.

## Materials and what each one means

Every material carries a fixed meaning. Do not use one decoratively.

- **Wood** — structure. The body of anything the player owns.
- **Copper** — the parts a hand touches: pulls, corner brackets.
- **Iron** — the parts that hold weight: straps, braces, shelf lips.
- **Paper** — anything catalogued: index cards, labels, an open book.

If a detail cannot be justified by one of those four, delete it.

## Recurring motifs

These tie the family together and must appear on future blocks:

- Copper corner brackets at the two front corners of a work surface.
- A three-pixel copper pull with a one-pixel highlight along its top.
- Beige index cards with two or three written marks.
- A vertical iron strap near the front edge of a carcass side.
- A dark one-pixel seam under every drawer.

No block carries a logo.

## Model rules

- Cuboids only. Four to six elements is the target; a dozen is too many.
- Author facing **north**; the blockstate supplies `y` rotation for the other
  three horizontal facings. Never author four separate models.
- `getStateForPlacement` faces the block's front at the player.
- Omit any face that is coplanar with another element's face. The catalogue
  shelf and the open book both sit on the work surface at y=11, so both drop
  their `down` face. This is the project's z-fighting policy, not a micro-
  optimisation.
- Blocks that are not full cubes must pass `.noOcclusion()` in their
  `BlockBehaviour.Properties`, or neighbouring faces get culled wrongly.
- Collision and outline shapes approximate the silhouette with two or three
  boxes, never a swarm of micro-boxes, and never an automatic full cube when a
  significant volume is visibly empty.
- No idle animation. No spinning gears, no blinking light, no permanent
  particles. An in-use state, if ever needed, must be a small physical change
  such as a raised card.

## Texture rules

- 16x16 per block face. Author each texture in **block-face space**: draw the
  whole 16x16 face, then window the UV rectangle to the part an element
  occupies. Pixel density then stays 1:1 with the block and nothing stretches.
- Watch the axis on top faces: `v` maps to **z**, and row 0 is the *front*
  (z=0). Putting the front detailing on row 15 hides it under the back shelf.
- Hand-placed pixels only. No gradients, no blur, no anti-aliasing, no noise,
  no downscaled photography.
- Keep strong horizontal banding on tall faces. It is what survives at 16
  blocks' distance.
- Prefer few bright accents. Paper is the loudest colour in the palette, so
  more than two or three paper spots per face turns to visual mush.
- Side faces should stay mirror-safe: vertical straps and horizontal grain read
  correctly whichever way the UV is sampled, asymmetric props do not.

## Future blocks

**Storage Inbox** — the receiving counterpart. The terminal is *consultation*;
the inbox is *intake and sorting*. It should read as a warehouse mail slot:
same wood carcass, same copper pulls, but with an open chute or hopper mouth
facing up or forward, and a bank of labelled pigeonholes instead of a card
catalogue. Keep it lower than the terminal so the two do not compete when
placed side by side, and give it the same copper corner brackets.

Whatever comes after, it must satisfy: same wood, same copper pulls, same beige
labels, same iron straps. Someone should be able to see three Chestwise blocks
in a screenshot and know they belong to one mod.

## Source files

- `art/generate_textures.py` — the block textures as explicit pixel maps.
- `art/generate_icons.py` — mod icon draft and GUI iconography.
- `art/build_bbmodel.py` — derives the Blockbench source from the shipped model
  so the two cannot drift.
- `art/preview_model.py` — orthographic render of the model straight from its
  JSON, for checking UV windows without launching the game.
- `art/blockbench/storage_terminal.bbmodel` — Blockbench source, textures
  embedded.

Regenerate everything with:

```shell
python art/generate_textures.py
python art/generate_icons.py
python art/build_bbmodel.py
```
