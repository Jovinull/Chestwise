# Asset provenance

## Storage Terminal block

`assets/chestwise/textures/block/storage_terminal_*.png` and
`art/blockbench/storage_terminal.bbmodel` are original assets authored for
Chestwise on 2026-08-26. No image generator, photograph, upscaler, or
third-party texture pack was involved: every pixel is placed explicitly by the
character grids in `art/generate_textures.py`, and the Blockbench source is
derived from the shipped model by `art/build_bbmodel.py`.

The design brief and the rules the art follows are recorded in
[`art-direction.md`](art-direction.md). No vanilla texture is copied, and the
model deliberately avoids the terminal language of Applied Energistics 2,
Refined Storage, and Tom's Simple Storage.

`art/gui_icons.png` and `art/mod_icon_draft_*.png` are drafts produced the same
way by `art/generate_icons.py`. They are not shipped inside the jar: the
terminal's buttons are still text, and the mod icon draft is a proposal that has
not replaced `icon.png`.

## Project icon

`src/main/resources/assets/chestwise/icon.png` is an original 256 × 256 raster
asset generated for Chestwise on 2026-08-26 with OpenAI image generation. It
was visually inspected at its original resolution before inclusion. It contains
no text, third-party logo, or copied game UI.

Final generation prompt:

> Use case: stylized-concept. Asset type: Minecraft mod icon, readable at 32x32
> and 128x128. Primary request: an original square icon representing Chestwise:
> a warm wooden storage chest whose front has a small embedded vanilla-inspired
> inventory search panel, suggesting that physical storage itself is searchable.
> Subject: one centered wooden chest, simple brass latch, subtle small
> magnifying-glass motif integrated into latch or panel. Style: polished
> pixel-art game UI icon, blocky Minecraft-compatible but original, crisp hard
> edges. Warm early-game palette: oak, charcoal, brass, and a tiny teal accent.
> No text, cables, power, disks, sci-fi elements, logos, trademarked UI, or
> watermark. Avoid direct imitation of Minecraft's chest, AE2, Refined Storage,
> or Tom's Simple Storage.

The generated working source was saved outside the repository at:

```text
C:\Users\felip\.codex\generated_images\01a03ae7-4191-7db3-9c59-54903bb3a7ed\exec-635f2511-2fc1-49d0-a67a-dbfddd59a5aa.png
```
