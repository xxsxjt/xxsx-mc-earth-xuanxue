"""Replace createBodyLayer in each beast model with the generated version."""

from __future__ import annotations

import io
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "neoforge-26.1.2" / "src" / "main" / "java" / "com" / "xxsx" / "earth" / "xuanxue" / "client" / "model"
GENERATED = ROOT / "tmp" / "generated_layers.txt"


def extract_blocks(text: str) -> dict[str, str]:
    blocks = {}
    for chunk in text.split("// ===== ")[1:]:
        name, _, body = chunk.partition(" =====")
        blocks[name.strip()] = body.strip("\n")
    return blocks


def replace_layer(source: str, new_layer: str) -> str:
    start = source.index("    public static LayerDefinition createBodyLayer() {")
    depth = 0
    end = start
    for i in range(start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    return source[:start] + new_layer + source[end:]


def main() -> None:
    blocks = extract_blocks(io.open(GENERATED, encoding="utf-8").read())
    for model_name, layer in blocks.items():
        path = MODELS / f"{model_name}.java"
        source = io.open(path, encoding="utf-8").read()
        updated = replace_layer(source, layer)
        io.open(path, "w", encoding="utf-8", newline="\n").write(updated)
        print("patched", model_name, "->", len(layer), "chars")


if __name__ == "__main__":
    main()
