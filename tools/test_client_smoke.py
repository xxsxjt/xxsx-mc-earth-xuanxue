"""Launch the dev client briefly and watch for the model-construction crash.

The reported symptom was a black screen caused by EntityModel construction throwing during
resource reload. This script runs the client, waits for the main menu or a crash, then
reports the decisive log lines and shuts the client down.
"""

from __future__ import annotations

import io
import os
import re
import subprocess
import time
from pathlib import Path

ROOT = Path(r'D:\AGENT\WORKSPACES\Projects\mc-agent\earth-series\xuanhuan_earth\neoforge-26.2')
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\client_smoke.log')

CRASH_MARKERS = [
    'NoSuchElementException',
    'abortResourcePackRecovery',
    'Cannot find part',
    'Exception in thread "Render thread"',
    'Failed to create model',
    'xuanhuan_earth',
]
READY_MARKERS = [
    'Backend library:',
    'OpenAL initialized',
    'Created:',
    'Narrator',
    'Sound engine started',
]


def main() -> None:
    env = dict(os.environ)
    env['JAVA_HOME'] = r'D:\Java\jdk-25'
    proc = subprocess.Popen(
        ['cmd.exe', '/c', str(ROOT / 'gradlew.bat'), '--offline', 'runClient'],
        cwd=str(ROOT), env=env,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, encoding='utf-8', errors='replace', bufsize=1)

    lines: list[str] = []
    deadline = time.time() + 210
    saw_model_error = False
    reached_menu = False

    while time.time() < deadline:
        line = proc.stdout.readline()
        if not line:
            break
        lines.append(line)
        low = line.lower()
        if 'nosuchelementexception' in low or 'cannot find part' in low \
                or 'abortresourcepackrecovery' in low:
            saw_model_error = True
        if 'openal initialized' in low or 'sound engine started' in low:
            reached_menu = True
            break

    # Give the client a moment, then shut it down.
    time.sleep(2)
    for child in ('javaw.exe', 'java.exe'):
        subprocess.run(['taskkill', '/F', '/IM', child, '/T'],
                       capture_output=True, text=True)
    try:
        proc.wait(timeout=30)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))

    print('lines captured:', len(lines))
    print('reached sound/menu stage:', reached_menu)
    print('model construction error seen:', saw_model_error)
    print('--- decisive lines ---')
    for line in lines:
        if any(k.lower() in line.lower() for k in
               ('nosuchelement', 'cannot find part', 'abortresourcepackrecovery',
                'failed to create model', 'entitymodel')):
            print('  ', line.strip()[:200])
    print('--- xuanhuan errors ---')
    for line in lines:
        if 'xuanhuan' in line.lower() and ('error' in line.lower() or 'exception' in line.lower()):
            print('  ', line.strip()[:200])


if __name__ == '__main__':
    main()
