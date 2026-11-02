"""Dump the PartDefinition tree and constructor getChild paths for each model."""

from __future__ import annotations

import io
import re
from pathlib import Path

ROOT = Path(r'D:\AGENT\WORKSPACES\Projects\mc-agent\earth-series\xuanhuan_earth')
MODELS = ROOT / 'neoforge-26.2' / 'src' / 'main' / 'java' / 'com' / 'xxsx' / 'xuanhuanearth' / 'client' / 'model'

PATTERN = re.compile(
    r'(?:PartDefinition\s+(\w+)\s*=\s*)?(\w+)\.addOrReplaceChild\(\s*"([a-z0-9_]+)"', re.S)


def main() -> None:
    for path in sorted(MODELS.glob('*.java')):
        source = io.open(path, encoding='utf-8').read()
        if 'ModelPart root' not in source:
            continue
        print('=' * 12, path.stem)
        print('-- registrations --')
        for m in PATTERN.finditer(source):
            assigned, parent, child = m.group(1), m.group(2), m.group(3)
            print(f'   {parent}.add("{child}")  -> {assigned or "(inline)"}')
        print('-- constructor --')
        i = source.find(f'public {path.stem}(ModelPart root)')
        j = source.find('public static LayerDefinition')
        print(source[i:j].rstrip())


if __name__ == '__main__':
    main()
