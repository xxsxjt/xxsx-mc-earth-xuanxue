# Texture Prompt Notes

First polished block-texture pass for `Xuanhuan Earth / 玄幻地球` used `gpt-image-2` through the local imagegen CLI and the DPAPI-encrypted image key. Raw generated images are kept under ignored `tmp/imagegen/`; final downscaled PNGs are packaged under:

```text
neoforge-26.1.2/src/main/resources/assets/earth_xuanxue/textures/block/
```

Prompt principles used:

- Minecraft block exterior texture first, not illustration or cutaway.
- Orthographic square tile, no perspective scene.
- No text, no pseudo-letters, no watermark, no UI.
- Readable after downscaling to 64x64.
- Xuanhuan flavor sits on physical materials: rock, soil, ceramic, wood, metal inlay, crystal nodes.
- Machine/workstation faces should expose believable exterior details such as furnace mouth, work surface, inlay, vents and crystal nodes.

Generated first:

- `spirit_vein_node`: dark deepslate host rock with violet crystal seams.
- `spirit_spring_stone`: pale wet limestone/prismarine-like rock with aqua mineral fissures.
- `spirit_soil`: dark fertile loam with roots, humus and faint qi filaments.
- `alchemy_furnace_front`: black ceramic and bronze furnace face with warm mouth glow.
- `talisman_table_top`: dark wood ritual table with parchment, cinnabar ink and gold inlay.
- `spirit_array_core_top`: carved stone array core with copper-gold inlay and violet crystal nodes.

Second item-icon pass:

- Generated with `gpt-image-2` from `tmp/imagegen/xuanhuan-items-20260707/xuanhuan-item-prompts.jsonl`.
- Raw sources remain under ignored `tmp/imagegen/xuanhuan-items-20260707/raw/`.
- Final icons are `32x32` transparent PNGs in `assets/earth_xuanxue/textures/item/`.
- Pillow was used only for chroma-key cleanup, cropping, downscaling and contact-sheet validation.
- Replaced vanilla placeholder item models for manuals, talisman materials, spirit compass, spirit spring bottle, spirit grass, spirit crystal shard, spirit iron blank and qi recovery pill.
