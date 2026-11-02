"""Verify every EntityModel's constructor getChild paths against its createBodyLayer tree.

A getChild path that does not exist throws NoSuchElementException while the EntityModel is
built during a resource reload, which blacks out the client. This check catches that before
launch.

The tree is keyed by *part name*, not by Java variable name: generated code often binds
`PartDefinition tail_tail_mid = tail.addOrReplaceChild("tail_mid", ...)`, and getChild must
use "tail_mid".
"""

from __future__ import annotations

import io
import re
import sys
from pathlib import Path

ROOT = Path(r'D:\AGENT\WORKSPACES\Projects\mc-agent\earth-series\xuanhuan_earth')
MODELS = ROOT / 'neoforge-26.2' / 'src' / 'main' / 'java' / 'com' / 'xxsx' / 'xuanhuanearth' / 'client' / 'model'

REGISTRATION = re.compile(
    r'(?:PartDefinition\s+(\w+)\s*=\s*)?(\w+)\.addOrReplaceChild\(\s*"([a-z0-9_]+)"', re.S)
ASSIGNMENT = re.compile(r'this\.(\w+)\s*=\s*([^;]+);', re.S)
GET_CHILD = re.compile(r'(\w+)\.getChild\(\s*"([a-z0-9_]+)"\s*\)')


def build_tree(source: str) -> tuple[dict[str, set[str]], dict[str, str]]:
    """Return (part name -> child part names, java variable -> part name)."""
    children: dict[str, set[str]] = {}
    var_to_part: dict[str, str] = {'root': 'root'}
    for m in REGISTRATION.finditer(source):
        assigned, parent_var, child_name = m.group(1), m.group(2), m.group(3)
        parent_part = var_to_part.get(parent_var, parent_var)
        children.setdefault(parent_part, set()).add(child_name)
        children.setdefault(child_name, set())
        if assigned:
            var_to_part[assigned] = child_name
    return children, var_to_part


def check(source: str, children: dict[str, set[str]], var_to_part: dict[str, str]) -> list[str]:
    problems = []
    # Field name -> part name, learned from earlier assignments in the constructor, so
    # `this.tailMid.getChild(...)` resolves to the part "tail_mid".
    field_to_part: dict[str, str] = {}
    for m in ASSIGNMENT.finditer(source):
        field, expr = m.group(1), m.group(2)
        if 'getChild(' not in expr:
            # this.body = root.getChild("body") was already handled below; plain fields are
            # not tracked because they never appear as a getChild base.
            continue
        base_match = re.match(r'\s*(?:this\.)?(\w+)', expr)
        if not base_match:
            continue
        base_var = base_match.group(1)
        if base_var == 'new':
            continue
        current = field_to_part.get(base_var) or var_to_part.get(base_var, base_var)
        for step in GET_CHILD.finditer(expr):
            name = step.group(2)
            available = children.get(current, set())
            if name not in available:
                problems.append(
                    f'  this.{field}: getChild("{name}") not found under part '
                    f'"{current}" (has {sorted(available)})')
                current = None
                break
            current = name
        if current is not None:
            field_to_part[field] = current
    return problems


def main() -> None:
    failures = 0
    for path in sorted(MODELS.glob('*.java')):
        source = io.open(path, encoding='utf-8').read()
        if 'ModelPart root' not in source:
            continue
        children, var_to_part = build_tree(source)
        problems = check(source, children, var_to_part)
        print(('OK   ' if not problems else 'FAIL ') + path.stem)
        for p in problems:
            print(p)
        if problems:
            failures += 1
    print('failures:', failures)
    sys.exit(1 if failures else 0)


if __name__ == '__main__':
    main()
