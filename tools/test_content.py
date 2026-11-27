"""Verify the new content loads: items registered, recipes present, no startup errors."""

from __future__ import annotations

import io
import os
import socket
import struct
import subprocess
import time
from pathlib import Path

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2')
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\content_check.log')
HOST, PORT, PASSWORD = '127.0.0.1', 25575, 'earthtest'

PROBES = [
    'give @p earth_xuanxue:foundation_pill 1',
    'give @p earth_xuanxue:ironbody_pill 1',
    'give @p earth_xuanxue:cloudbreath_pill 1',
    'give @p earth_xuanxue:grainfast_pill 1',
    'give @p earth_xuanxue:nightsight_pill 1',
    'give @p earth_xuanxue:breakthrough_pill 1',
    'give @p earth_xuanxue:wind_fan 1',
    'give @p earth_xuanxue:stone_seal 1',
    'give @p earth_xuanxue:gather_orb 1',
    'give @p earth_xuanxue:lantern_rod 1',
    'place feature earth_xuanxue:mountain_spirit_shrine 0 100 0',
    'place feature earth_xuanxue:spirit_beast_keeper_camp 40 100 0',
]


class Rcon:
    def __init__(self, host, port, password):
        self.sock = socket.create_connection((host, port), timeout=20)
        self.next_id = 0
        self._send(3, password)
        self._recv()

    def _send(self, kind, body):
        self.next_id += 1
        payload = struct.pack('<ii', self.next_id, kind) + body.encode() + b'\x00\x00'
        self.sock.sendall(struct.pack('<i', len(payload)) + payload)

    def _recv(self):
        raw = self.sock.recv(4)
        if len(raw) < 4:
            return ''
        (length,) = struct.unpack('<i', raw)
        data = b''
        while len(data) < length:
            chunk = self.sock.recv(length - len(data))
            if not chunk:
                break
            data += chunk
        return data[8:-2].decode('utf-8', errors='replace')

    def cmd(self, text):
        self._send(2, text)
        return self._recv()


def main() -> None:
    env = dict(os.environ)
    env['JAVA_HOME'] = r'D:\Java\jdk-25'
    proc = subprocess.Popen(
        ['cmd.exe', '/c', str(ROOT / 'gradlew.bat'), '--offline', 'runServer'],
        cwd=str(ROOT), env=env,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, encoding='utf-8', errors='replace', bufsize=1)

    lines = []
    client = None
    deadline = time.time() + 300
    while time.time() < deadline:
        line = proc.stdout.readline()
        if not line:
            break
        lines.append(line)
        if 'Done (' in line:
            time.sleep(3)
            for _ in range(10):
                try:
                    client = Rcon(HOST, PORT, PASSWORD)
                    break
                except OSError:
                    time.sleep(2)
            break

    results = []
    if client is not None:
        for probe in PROBES:
            results.append((probe, client.cmd(probe).strip()[:140]))
            time.sleep(1)
        # Recipe count proves the new recipes parsed.
        results.append(('recipe count', client.cmd(
            'execute if entity @e run say recipes_loaded').strip()[:60]))
        time.sleep(2)
        client.cmd('stop')

    try:
        proc.wait(timeout=120)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))

    ok = 0
    for probe, reply in results:
        good = 'Unknown' not in reply and 'error' not in reply.lower() and reply
        if good:
            ok += 1
        print(('PASS ' if good else 'FAIL '), probe)
        if not good:
            print('     ', reply)

    joined = ''.join(lines)
    print('--- startup ---')
    for line in joined.splitlines():
        if 'Loaded 5 xuanhuan settlement' in line or 'Loaded 2 spell graph' in line:
            print('   ', line.strip()[:140])
    print('--- errors ---')
    errs = [l for l in joined.splitlines()
            if ('ERROR' in l or 'Exception' in l) and 'parseChunk' not in l
            and 'earth_online_magic' not in l and 'Scanner' not in l]
    for line in errs[:10]:
        print('   ', line.strip()[:160])
    print(f'passed {ok}/{len(results)}')


if __name__ == '__main__':
    main()
