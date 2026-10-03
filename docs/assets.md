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

## Project icon

`src/main/resources/assets/chestwise/icon.png` is an original 256 x 256 asset
authored for Chestwise, drawn as explicit rectangles by
`art/generate_icons.py` and exported at 32 x 32 before a nearest-neighbour
upscale, so every pixel stays hard-edged. It shows a catalogue card lifted out of
a drawer rather than the whole block, which stays readable at the 32-64 px sizes
used by mod lists and store pages. No image generator, photograph, or upscaler
was involved, and it contains no text, third-party logo, or copied game UI.

It replaced an earlier raster icon that had been produced with OpenAI image
generation on 2026-08-26; that asset is no longer used or shipped.

`src/main/resources/assets/chestwise/textures/gui/terminal_icons.png` contains
the single 16 x 16 search glyph used beside the search field. It is authored by
the same explicit-rectangle generator. No unused GUI glyphs ship in the runtime
artifact.
