"""Place a settlement, then read the anchor block entity NBT to see if it initialised."""

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
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\anchor_nbt.log')
HOST, PORT, PASSWORD = '127.0.0.1', 25575, 'earthtest'


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

    out = []
    if client is not None:
        client.cmd('forceload add -64 -64 64 64')
        client.cmd('place feature earth_xuanxue:spirit_spring_village 0 100 0')
        time.sleep(3)
        # Find the anchor by scanning down for the block, then read its NBT.
        for y in range(110, 40, -1):
            probe = client.cmd(f'execute if block 0 {y} 0 earth_xuanxue:settlement_anchor')
            if probe.strip() == 'Test passed':
                out.append(('anchor_y', str(y)))
                out.append(('nbt_now', client.cmd(f'data get block 0 {y} 0')))
                time.sleep(12)
                out.append(('nbt_later', client.cmd(f'data get block 0 {y} 0')))
                break
        else:
            out.append(('anchor_y', 'not found'))
        time.sleep(3)
        client.cmd('save-all')
        time.sleep(2)
        client.cmd('stop')

    try:
        proc.wait(timeout=120)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))
    for label, text in out:
        print(label, '=', (text or '').strip()[:400])


if __name__ == '__main__':
    main()
