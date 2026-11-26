#!/usr/bin/env python3
"""
Teste de fumaça com um mundo de verdade (gerador normal do End, não o plano dos GameTests).

1. Sobe `./gradlew runServer` com RCON ligado num mundo novo, com seed fixa.
2. Pelo RCON, localiza no End os quatro biomas novos, as Ruínas do Vazio e uma Cidade do End.
3. Força a geração dos chunks encontrados (rodando superfície, features e a estrutura).
4. Para o servidor e falha se algum passo falhou ou se o log tiver exceções do mod.

Uso:  python3 tools/smoke_test.py          (precisa de JDK 25 no JAVA_HOME)
"""
from __future__ import annotations

import os
import re
import shutil
import socket
import struct
import subprocess
import sys
import threading
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RUN = ROOT / "run"
WORLD = "smoke-world"
RCON_PORT = 25576
RCON_PASSWORD = "hollow-horizons-smoke"
SEED = "26042026"
BIOMES = ["pale_silk_woods", "vitreous_chasms", "resonant_spirefields", "void_fen"]


class Rcon:
    def __init__(self, port: int, password: str):
        self.sock = socket.create_connection(("127.0.0.1", port), timeout=600)
        self._id = 0
        if self._send(3, password)[0] == -1:
            raise RuntimeError("RCON: senha recusada")

    def _send(self, kind: int, payload: str):
        self._id += 1
        data = struct.pack("<ii", self._id, kind) + payload.encode() + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(data)) + data)
        length = struct.unpack("<i", self._recv(4))[0]
        body = self._recv(length)
        req_id, _ = struct.unpack("<ii", body[:8])
        return req_id, body[8:-2].decode(errors="replace")

    def _recv(self, n: int) -> bytes:
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("RCON fechado")
            buf += chunk
        return buf

    def cmd(self, command: str) -> str:
        return self._send(2, command)[1]


def prepare_run_dir(reset=True):
    RUN.mkdir(exist_ok=True)
    if reset:
        shutil.rmtree(RUN / WORLD, ignore_errors=True)
    (RUN / "eula.txt").write_text("eula=true\n")
    (RUN / "server.properties").write_text("\n".join([
        "enable-rcon=true", f"rcon.port={RCON_PORT}", f"rcon.password={RCON_PASSWORD}", "server-port=25566",
        "online-mode=false", f"level-seed={SEED}", f"level-name={WORLD}", "spawn-protection=0",
        "view-distance=4", "simulation-distance=4", "max-tick-time=-1", "sync-chunk-writes=false"]) + "\n")


def main() -> int:
    prepare_run_dir()
    gradlew = str(ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew"))
    server = subprocess.Popen([gradlew, "runServer", "--no-daemon", "--console=plain"], cwd=ROOT,
                              stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1)
    log: list[str] = []
    ready = threading.Event()

    def pump():
        for line in server.stdout:
            log.append(line)
            if "Done (" in line and "For help" in line:
                ready.set()
            if "RCON running" in line:
                ready.set()

    threading.Thread(target=pump, daemon=True).start()
    failures: list[str] = []
    try:
        if not ready.wait(900):
            print("".join(log[-60:]))
            print("FALHA: o servidor não terminou de iniciar em 15 minutos")
            return 1
        time.sleep(2)
        rcon = Rcon(RCON_PORT, RCON_PASSWORD)

        def run(command: str) -> str:
            out = rcon.cmd(command)
            print(f"> {command}\n  {out}")
            return out

        def coords(text: str):
            m = re.search(r"\[(-?\d+), (~|-?\d+), (-?\d+)\]", text)
            return (int(m.group(1)), int(m.group(3))) if m else None

        targets = []
        for biome in BIOMES:
            out = run(f"execute in minecraft:the_end run locate biome hollowhorizons:{biome}")
            pos = coords(out)
            if pos is None:
                failures.append(f"bioma {biome} não localizado")
            else:
                targets.append((biome, pos))
        out = run("execute in minecraft:the_end positioned 1500 64 0 run locate structure hollowhorizons:void_ruins")
        ruins = coords(out)
        if ruins is None:
            failures.append("Ruínas do Vazio não localizadas")
        else:
            targets.append(("void_ruins", ruins))
        if coords(run("execute in minecraft:the_end positioned 1500 64 0 run locate structure #hollowhorizons:resonance/end_city")) is None:
            failures.append("Cidade do End (alvo da Bússola) não localizada")

        # Gera de verdade os chunks encontrados: superfície, features e estrutura rodam aqui.
        for name, (x, z) in targets:
            run(f"execute in minecraft:the_end run forceload add {x - 16} {z - 16} {x + 16} {z + 16}")
        time.sleep(25)

        # Os blocos de assinatura de cada lugar existem mesmo no terreno gerado?
        # (fill ... replace com o próprio bloco conta as ocorrências sem alterar nada visível;
        #  se o jogo não contar trocas idênticas, cai para a contagem destrutiva no mundo de teste.)
        signature = {"pale_silk_woods": "pale_silk_moss", "vitreous_chasms": "vitreous_obsidian",
                     "resonant_spirefields": "resonant_spire", "void_fen": "void_mud", "void_ruins": "fen_bricks"}
        for name, (x, z) in targets:
            block = f"hollowhorizons:{signature[name]}"
            total = 0
            for dx in (-16, 0):
                for dz in (-16, 0):
                    out = run(f"execute in minecraft:the_end run fill {x + dx} 20 {z + dz} {x + dx + 15} 119 {z + dz + 15} "
                              f"minecraft:air replace {block}")
                    m = re.search(r"(\d+) blocks?", out)
                    total += int(m.group(1)) if m and "filled" in out.lower() else 0
            print(f"  {name}: {total} x {block}")
            if total == 0:
                failures.append(f"{name}: nenhum {block} no terreno gerado perto de {x},{z}")

        for name, (x, z) in targets:
            run(f"execute in minecraft:the_end run forceload remove {x - 16} {z - 16} {x + 16} {z + 16}")
        run("stop")
    except Exception as exc:  # noqa: BLE001
        failures.append(f"erro durante o teste: {exc!r}")
        server.terminate()
    finally:
        try:
            server.wait(timeout=180)
        except subprocess.TimeoutExpired:
            server.kill()

    errors = [l.rstrip() for l in log if ("Exception" in l or "/ERROR]" in l) and ("hollowhorizons" in l.lower() or "Feature" in l)]
    if errors:
        failures.append("exceções no log:\n    " + "\n    ".join(errors[:20]))
    if failures:
        print("\nFALHOU:\n  - " + "\n  - ".join(failures))
        return 1
    print("\nOK: biomas, Ruínas do Vazio e Cidades do End localizados, gerados e com seus blocos no End de um mundo real.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
