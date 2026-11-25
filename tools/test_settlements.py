"""Start the dev server, run placement commands, and capture the results.

Uses a fresh flat world so the test is about settlement generation, not terrain, and pipes
commands through stdin so no player login is needed.
"""

from __future__ import annotations

import io
import os
import re
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2')
RUNS = ROOT / 'runs' / 'server'
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\settlement_test.log')

COMMANDS = [
    # Force one of each type near spawn, twice, to compare two rolls of the same type.
    'forceload add -64 -64 64 64',
    'place feature earth_xuanxue:wandering_cultivator_market ~ ~ ~',
    'place feature earth_xuanxue:wandering_cultivator_market ~20 ~ ~20',
    'place feature earth_xuanxue:spirit_spring_village ~-30 ~ ~',
    'place feature earth_xuanxue:spirit_spring_village ~-30 ~ ~-25',
    'place feature earth_xuanxue:sect_frontier_outpost ~ ~ ~40',
    'place feature earth_xuanxue:mountain_spirit_shrine ~40 ~ ~',
    'place feature earth_xuanxue:spirit_beast_keeper_camp ~ ~ ~-40',
    'list',
    'stop',
]


def main() -> None:
    env = dict(os.environ)
    env['JAVA_HOME'] = r'D:\Java\jdk-25'
    proc = subprocess.Popen(
        ['cmd.exe', '/c', str(ROOT / 'gradlew.bat'), '--offline', 'runServer'],
        cwd=str(ROOT), env=env,
        stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, encoding='utf-8', errors='replace', bufsize=1)

    lines: list[str] = []
    sent = False
    deadline = time.time() + 300
    started = False

    while time.time() < deadline:
        line = proc.stdout.readline()
        if not line:
            break
        lines.append(line)
        if 'Done (' in line and not started:
            started = True
            time.sleep(3)
            for cmd in COMMANDS:
                proc.stdin.write(cmd + '\n')
                proc.stdin.flush()
                time.sleep(2)
            sent = True
        if sent and 'All dimensions are saved' in line:
            break

    try:
        proc.wait(timeout=60)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))
    print('log', LOG, 'lines', len(lines))
    print('started', started, 'commands sent', sent)

    joined = ''.join(lines)
    for pattern in ('Initialized xuanhuan settlement', 'Loaded 5 xuanhuan settlement',
                    'Loaded 2 spell graph', 'ERROR', 'Exception'):
        hits = [l.strip() for l in joined.splitlines() if pattern in l]
        print(f'--- {pattern}: {len(hits)}')
        for hit in hits[:12]:
            print('   ', hit[:160])


if __name__ == '__main__':
    main()
