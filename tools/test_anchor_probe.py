"""Place one of each settlement, then probe the anchor block and its block entity."""

from __future__ import annotations

import io
import os
import socket
import struct
import subprocess
import time
from pathlib import Path

ROOT = Path(r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2')
LOG = Path(r'C:\Users\xxsx\AppData\Local\Temp\opencode\settlement_probe.log')
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


COMMANDS = [
    'forceload add -64 -64 64 64',
    'place feature earth_xuanxue:wandering_cultivator_market 0 100 0',
    'place feature earth_xuanxue:spirit_spring_village -40 100 0',
    'place feature earth_xuanxue:sect_frontier_outpost 0 100 60',
    'place feature earth_xuanxue:mountain_spirit_shrine 60 100 0',
    'place feature earth_xuanxue:spirit_beast_keeper_camp 0 100 -60',
    'setblock 0 100 0 earth_xuanxue:settlement_anchor',
    'execute if block 0 100 0 earth_xuanxue:settlement_anchor run say ANCHOR_OK',
    'data get block 0 100 0',
    'data get block -40 100 0',
]


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
        for cmd in COMMANDS:
            results.append((cmd, client.cmd(cmd).strip()[:400]))
            time.sleep(2)
        time.sleep(10)
        client.cmd('save-all')
        time.sleep(2)
        client.cmd('stop')

    try:
        proc.wait(timeout=120)
    except subprocess.TimeoutExpired:
        proc.kill()

    LOG.parent.mkdir(parents=True, exist_ok=True)
    io.open(LOG, 'w', encoding='utf-8').write(''.join(lines))

    for cmd, reply in results:
        print('>>', cmd)
        print('  ', reply)
    print('--- settlement init log ---')
    for line in ''.join(lines).splitlines():
        if 'Initialized xuanhuan settlement' in line:
            print('   ', line.strip()[:200])


if __name__ == '__main__':
    main()
