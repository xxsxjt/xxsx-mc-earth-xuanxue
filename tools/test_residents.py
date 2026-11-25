"""Place each settlement type and confirm residents spawn inside the site, not in walls."""

from __future__ import annotations

import io
import os
import re
import socket
import struct
import subprocess
import time
from pathlib import Path

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2')
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\resident_check.log')
HOST, PORT, PASSWORD = '127.0.0.1', 25575, 'earthtest'

PLACES = [
    ('wandering_cultivator_market', 0, 0),
    ('spirit_spring_village', 60, 0),
    ('sect_frontier_outpost', -60, 0),
    ('mountain_spirit_shrine', 0, 60),
    ('spirit_beast_keeper_camp', 0, -60),
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

    report = []
    if client is not None:
        client.cmd('forceload add -128 -128 128 128')
        for kind, x, z in PLACES:
            client.cmd(f'place feature earth_xuanxue:{kind} {x} 100 {z}')
        # Let the anchors finish their 40-tick warmup, then count residents.
        time.sleep(15)
        for kind, x, z in PLACES:
            out = client.cmd(
                f'execute as @e[type=earth_xuanxue:cultivation_settler,'
                f'distance=..40] run data get entity @s Role 2>/dev/null')
            count = client.cmd(
                f'execute positioned {x} 100 {z} run data get entity @e[type=earth_xuanxue:cultivation_settler,'
                f'limit=1,sort=nearest,distance=..40]')
            residents = client.cmd(
                f'execute positioned {x} 100 {z} run data get entity '
                f'@e[type=earth_xuanxue:cultivation_settler,limit=5,sort=nearest,distance=..40]')
            report.append((kind, (residents or '').strip()[:220]))
        time.sleep(4)
        client.cmd('save-all')
        time.sleep(2)
        client.cmd('stop')

    try:
        proc.wait(timeout=120)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))

    joined = ''.join(lines)
    print('--- anchor init log ---')
    for line in joined.splitlines():
        if 'Initialized xuanhuan settlement' in line:
            print('   ', line.strip()[:200])
    print('--- residents near each site ---')
    for kind, text in report:
        print(kind, '->', text[:180])
    print('--- errors ---')
    for line in joined.splitlines():
        if 'Exception' in line or ('ERROR' in line and 'parseChunk' not in line):
            print('   ', line.strip()[:180])


if __name__ == '__main__':
    main()
