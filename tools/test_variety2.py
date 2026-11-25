"""Place six spirit spring villages and read each anchor's NBT at its real ground height."""

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
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\settlement_variety2.log')
HOST, PORT, PASSWORD = '127.0.0.1', 25575, 'earthtest'

XS = [0, 40, -40, 0, 0, 80]
ZS = [0, 0, 0, 40, -40, 80]


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

    records = []
    if client is not None:
        client.cmd('forceload add -96 -96 96 96')
        for x, z in zip(XS, ZS):
            client.cmd(f'place feature earth_xuanxue:spirit_spring_village {x} 100 {z}')
            time.sleep(2)
            # The feature drops to the surface, so probe downward for the anchor.
            found = None
            for y in range(110, 40, -1):
                probe = client.cmd(f'execute if block {x} {y} {z} earth_xuanxue:settlement_anchor')
                if probe.strip() == 'Test passed':
                    found = y
                    break
            if found is None:
                records.append((f'{x},?,{z}', '?', '?', 'no anchor'))
                continue
            nbt = client.cmd(f'data get block {x} {found} {z}')
            palette = re.search(r'Palette: "([^"]*)"', nbt)
            layout = re.search(r'Layout: (\d+)', nbt)
            kind = re.search(r'SettlementType: "([^"]*)"', nbt)
            records.append((f'{x},{found},{z}',
                            palette.group(1) if palette else '?',
                            layout.group(1) if layout else '?',
                            kind.group(1) if kind else '?'))
        time.sleep(6)
        client.cmd('save-all')
        time.sleep(2)
        client.cmd('stop')

    try:
        proc.wait(timeout=120)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))

    print('pos              palette        layout  type')
    for row in records:
        print(f'{row[0]:<16} {row[1]:<14} {row[2]:<7} {row[3]}')
    palettes = {r[1] for r in records if r[1] != '?'}
    layouts = {r[2] for r in records if r[2] != '?'}
    print(f'distinct palettes={len(palettes)} layouts={len(layouts)} of {len(records)}')


if __name__ == '__main__':
    main()
