#!/usr/bin/env python3
"""
Gerador das texturas de Hollow Horizons.

Toda a arte aqui é ORIGINAL, desenhada pixel a pixel por código — nenhum PNG da Mojang é
copiado, editado ou redistribuído. O que segue o jogo é o *estilo*:

  * 16x16 para blocos e itens, 64x32 para camadas de equipamento;
  * paletas curtas (4-7 tons) em rampas de luz, com a luz vindo de cima à esquerda;
  * itens com contorno de 1 px no tom mais escuro do próprio material (nunca preto puro);
  * blocos que se repetem sem emenda e com ruído "à mão", não gradientes lisos;
  * transparência binária (recorte) em folhas e plantas, translucidez só onde é líquido.

Uso:  python3 tools/generate_textures.py      (requer Pillow)
Saída: src/main/resources/assets/hollowhorizons/textures/...
"""
from __future__ import annotations

import json
import math
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/hollowhorizons/textures"
CLEAR = (0, 0, 0, 0)


# --------------------------------------------------------------------------- utilidades

def hexc(value: str, alpha: int = 255) -> tuple[int, int, int, int]:
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


def ramp(*colors: str) -> list[tuple[int, int, int, int]]:
    """Rampa do mais escuro para o mais claro."""
    return [hexc(c) for c in colors]


def pick(r: list, t: float):
    t = max(0.0, min(0.9999, t))
    return r[int(t * len(r))]


class Canvas:
    def __init__(self, w: int = 16, h: int = 16, fill=CLEAR):
        self.w, self.h = w, h
        self.px = [[fill for _ in range(w)] for _ in range(h)]

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y][x]
        return CLEAR

    def set(self, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = c

    def setw(self, x, y, c):
        """Set com repetição (para texturas de bloco sem emenda)."""
        self.px[int(y) % self.h][int(x) % self.w] = c

    def filled(self, x, y) -> bool:
        return self.get(x, y)[3] > 0

    def line(self, x0, y0, x1, y1, c):
        x0, y0, x1, y1 = int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1))
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def outline(self, color, diagonal=False):
        """Pinta de `color` os pixels do contorno interno da forma (estilo de item vanilla)."""
        edge = []
        for y in range(self.h):
            for x in range(self.w):
                if not self.filled(x, y):
                    continue
                n = [(1, 0), (-1, 0), (0, 1), (0, -1)]
                if diagonal:
                    n += [(1, 1), (1, -1), (-1, 1), (-1, -1)]
                if any(not self.filled(x + dx, y + dy) for dx, dy in n):
                    edge.append((x, y))
        for x, y in edge:
            self.px[y][x] = color

    def image(self) -> Image.Image:
        im = Image.new("RGBA", (self.w, self.h))
        im.putdata([p for row in self.px for p in row])
        return im

    def save(self, rel: str):
        path = TEX / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        self.image().save(path)


def shade_shape(mask: list[tuple[int, int]], pal, light=(-1.0, -1.0), jitter=0.12, seed=0, cv: Canvas | None = None) -> Canvas:
    """Preenche uma máscara com uma rampa de cor iluminada de cima-esquerda."""
    rnd = random.Random(seed)
    cv = cv or Canvas()
    xs = [p[0] for p in mask]
    ys = [p[1] for p in mask]
    minx, maxx, miny, maxy = min(xs), max(xs), min(ys), max(ys)
    span = max(1, (maxx - minx) + (maxy - miny))
    for x, y in mask:
        t = 1.0 - ((x - minx) * -light[0] + (y - miny) * -light[1]) / span
        t = 0.15 + 0.75 * t + rnd.uniform(-jitter, jitter)
        cv.set(x, y, pick(pal, t))
    return cv


def disc(cx, cy, r):
    return [(x, y) for y in range(16) for x in range(16) if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= r * r]


def noise_block(pal, seed, blotch=0.35, size=16) -> Canvas:
    """Ruído de bloco 'pintado à mão': base + manchas de 1-3 px sem emenda."""
    rnd = random.Random(seed)
    cv = Canvas(size, size)
    mid = len(pal) // 2
    for y in range(size):
        for x in range(size):
            cv.set(x, y, pal[mid if rnd.random() > 0.4 else max(0, mid - 1)])
    for _ in range(int(size * size * blotch)):
        x, y = rnd.randrange(size), rnd.randrange(size)
        c = pal[rnd.randrange(len(pal))]
        cv.setw(x, y, c)
        if rnd.random() < 0.5:
            cv.setw(x + rnd.choice((-1, 1)), y, c)
        if rnd.random() < 0.3:
            cv.setw(x, y + 1, c)
    return cv


# --------------------------------------------------------------------------- paletas

SILK_BARK = ramp("#8a7fa3", "#a69cb8", "#bfb6cf", "#d4cde0", "#e6e1ee", "#f5f2fa")
SILK_WOOD = ramp("#b8a9cc", "#cbbedb", "#dccfe8", "#e9def2", "#f4edf9")
SILK_LEAF = ramp("#7380b4", "#929fd0", "#b0bce6", "#ccd6f4", "#e6ecfd", "#ffffff")
GLOW = hexc("#d9fbff")
END_STONE = ramp("#bdb67f", "#cdc68b", "#d9de98", "#e6eca8", "#f0f6b6")
VITREOUS = ramp("#07050c", "#0f0b18", "#171126", "#211836", "#2e2249")
VITREOUS_SHINE = ramp("#3d2e62", "#5a4790", "#8b74c9", "#c9b8f5")
PLASMA = ramp("#4a1380", "#6a1fb5", "#8a2be2", "#b45cff", "#d9a6ff", "#f3e1ff")
QUARTZ = ramp("#9b8db8", "#b6aacf", "#cfc5e3", "#e4dcf2", "#f3eefb", "#ffffff")
QUARTZ_TINT = ramp("#f1c7ef", "#c9f1ff")
MUD = ramp("#17131f", "#1e1a2b", "#262138", "#2e2843", "#3a3352", "#4a416a")
FEN = ramp("#1f272d", "#2a343b", "#34414a", "#3f4e58", "#4c5d68", "#5d707b")
FEN_MORTAR = hexc("#161c21")
LILY = ramp("#0f3c43", "#155660", "#1d727c", "#2a9199", "#45b3b5")
REED = ramp("#2a1238", "#3e1c53", "#56286f", "#73388f")
ANCHOR = ramp("#1a0f24", "#2d1f3d", "#43305c", "#5e4580", "#8c6bbd", "#b99ae6")
ENERGY = ramp("#9b4dff", "#c77dff", "#e3b8ff", "#f8ecff")
BOOT = ramp("#120f22", "#1b1830", "#2a2547", "#3b345f", "#51497f")
CYAN = ramp("#1f8fa3", "#3fc7da", "#6ef3ff", "#c6fbff")
END_ROD = ramp("#7d7489", "#a9a2b0", "#d8d2db", "#f3eef0", "#ffffff")
ECHO = ramp("#05303a", "#0a5060", "#068a95", "#29d4e0", "#9df7ff")


# --------------------------------------------------------------------------- blocos

def pale_silk_log():
    rnd = random.Random(11)
    cv = Canvas()
    for x in range(16):
        col = rnd.choice([2, 3, 3, 4])
        for y in range(16):
            cv.set(x, y, SILK_BARK[col])
    # sulcos verticais interrompidos (casca fibrosa, como seda)
    for groove in range(7):
        x = rnd.randrange(16)
        y0 = rnd.randrange(16)
        length = rnd.randint(3, 9)
        for i in range(length):
            cv.setw(x, y0 + i, SILK_BARK[1] if i not in (0, length - 1) else SILK_BARK[2])
            if rnd.random() < 0.25:
                cv.setw(x + 1, y0 + i, SILK_BARK[2])
    for _ in range(14):
        cv.setw(rnd.randrange(16), rnd.randrange(16), SILK_BARK[5])
    for _ in range(3):  # fios de luz presos à casca
        x, y = rnd.randrange(16), rnd.randrange(16)
        cv.setw(x, y, GLOW)
        cv.setw(x, y + 1, SILK_BARK[5])
    cv.save("block/pale_silk_log.png")

    top = Canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6.5:
                top.set(x, y, SILK_BARK[2 + (x * 7 + y * 3) % 2])
            else:
                ring = int(d) % 2
                top.set(x, y, SILK_WOOD[3 - ring] if d > 1 else SILK_WOOD[1])
    for i in range(1, 15):
        top.set(i, 1, SILK_BARK[1])
        top.set(1, i, SILK_BARK[1])
    top.set(7, 7, SILK_WOOD[0]); top.set(8, 8, SILK_WOOD[0])
    top.save("block/pale_silk_log_top.png")


def pale_silk_planks():
    rnd = random.Random(21)
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, SILK_WOOD[2 + (rnd.random() < 0.3)])
    for row in range(4):
        y_bottom = row * 4 + 3
        for x in range(16):
            cv.set(x, y_bottom, SILK_WOOD[0])
            cv.set(x, row * 4, SILK_WOOD[4] if rnd.random() < 0.6 else SILK_WOOD[3])
        seam = (row * 7 + 3) % 16
        for y in range(row * 4, row * 4 + 3):
            cv.set(seam, y, SILK_WOOD[0])
        for _ in range(4):  # veios
            x = rnd.randrange(16)
            cv.set(x, row * 4 + 1 + rnd.randrange(2), SILK_WOOD[1])
    cv.save("block/pale_silk_planks.png")


def pale_silk_leaves():
    rnd = random.Random(31)
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.80:
                cv.set(x, y, SILK_LEAF[rnd.choice([1, 2, 2, 3, 3, 4])])
    # folhas: pequenos losangos claros com sombra embaixo
    for _ in range(9):
        x, y = rnd.randrange(16), rnd.randrange(16)
        cv.setw(x, y, SILK_LEAF[5])
        cv.setw(x + 1, y, SILK_LEAF[4])
        cv.setw(x, y + 1, SILK_LEAF[1])
        cv.setw(x + 1, y + 1, SILK_LEAF[0])
    for _ in range(26):
        cv.setw(rnd.randrange(16), rnd.randrange(16), CLEAR)
    for _ in range(5):
        cv.setw(rnd.randrange(16), rnd.randrange(16), GLOW)
    cv.save("block/pale_silk_leaves.png")


def pale_silk_sapling():
    cv = Canvas()
    for y in range(9, 16):
        cv.set(7, y, SILK_BARK[2])
        cv.set(8, y, SILK_BARK[4])
    cv.set(6, 12, SILK_BARK[3]); cv.set(9, 11, SILK_BARK[4])
    leaves = set()
    for cx, cy, r in [(8, 5, 3.4), (4.5, 8, 2.4), (11.5, 7.5, 2.6), (7.5, 2.5, 2.0)]:
        leaves |= set(disc(cx, cy, r))
    shade_shape(sorted(leaves), SILK_LEAF[1:], seed=4, cv=cv)
    cv.outline(SILK_LEAF[0])
    for x, y in [(7, 4), (9, 6), (5, 8), (11, 7)]:
        cv.set(x, y, GLOW)
    cv.save("block/pale_silk_sapling.png")

def luminous_silk():
    cv = Canvas()
    c = (7.5, 7.5)
    threads = [hexc("#bfe3ff"), hexc("#e8f6ff"), hexc("#ffffff")]
    for k in range(8):
        a = k / 8 * math.tau + 0.2
        cv.line(c[0], c[1], c[0] + math.cos(a) * 8, c[1] + math.sin(a) * 8, threads[k % 2])
    for r, col in [(2.6, threads[2]), (4.8, threads[1]), (7.0, threads[0])]:
        pts = [(c[0] + math.cos(k / 8 * math.tau + 0.2) * r, c[1] + math.sin(k / 8 * math.tau + 0.2) * r) for k in range(8)]
        for i in range(8):
            x0, y0 = pts[i]
            x1, y1 = pts[(i + 1) % 8]
            # fios "caídos" entre os raios, como seda molhada de luz
            cv.line(x0, y0, (x0 + x1) / 2, (y0 + y1) / 2 + 0.8, col)
            cv.line((x0 + x1) / 2, (y0 + y1) / 2 + 0.8, x1, y1, col)
    for x, y in [(7, 7), (8, 8), (7, 8), (8, 7), (3, 12), (12, 3), (13, 12)]:
        cv.set(x, y, hexc("#ffffff"))
    cv.save("block/luminous_silk.png")


def pale_silk_moss():
    rnd = random.Random(41)
    top = Canvas()
    for y in range(16):
        for x in range(16):
            top.set(x, y, SILK_LEAF[rnd.choice([2, 3, 3, 4, 4, 5])])
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16)
        top.setw(x, y, SILK_LEAF[1]); top.setw(x + 1, y + 1, SILK_LEAF[1])
    for _ in range(4):
        top.setw(rnd.randrange(16), rnd.randrange(16), GLOW)
    top.save("block/pale_silk_moss_top.png")

    side = noise_block(END_STONE, 42, blotch=0.4)
    for x in range(16):
        drip = 3 + rnd.choice([0, 0, 1, 1, 2, 3])
        for y in range(drip):
            side.set(x, y, SILK_LEAF[rnd.choice([2, 3, 4, 5])])
        side.set(x, drip, SILK_LEAF[1])
    side.save("block/pale_silk_moss_side.png")


def vitreous_obsidian():
    rnd = random.Random(51)
    cv = noise_block(VITREOUS, 52, blotch=0.5)
    # faces de vidro: arestas diagonais brilhantes (a obsidiana comum não tem)
    for start in (-4, 5, 13):
        length = rnd.randint(5, 9)
        x, y = start, rnd.randrange(16)
        for i in range(length):
            cv.setw(x + i, y - i, VITREOUS_SHINE[1 + (i in (1, 2))])
            cv.setw(x + i + 1, y - i, VITREOUS[4])
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        cv.setw(x, y, VITREOUS_SHINE[3])
        cv.setw(x + 1, y, VITREOUS_SHINE[1])
    cv.save("block/vitreous_obsidian.png")


def crystal_cluster(path, body, edge, tip, seed):
    """Aglomerado em cruz: pontas largas com face clara à esquerda e escura à direita."""
    rnd = random.Random(seed)
    cv = Canvas()
    spikes = [(8, 15, 0.0, 14, 2.6), (3, 15, -1.5, 9, 1.8), (13, 15, 1.5, 10, 1.8), (5, 15, -0.5, 6, 1.4), (11, 15, 0.5, 6, 1.4)]
    for bx, by, lean, height, half in spikes:
        for i in range(height):
            t = i / height
            w = max(0.45, half * (1 - t ** 1.4))
            cx = bx + lean * t
            for x in range(int(cx - w) - 1, int(cx + w) + 2):
                off = x + 0.5 - cx
                if abs(off) <= w:
                    c = body[4] if off < -w * 0.3 else body[3] if off < 0.2 else body[1]
                    cv.set(x, by - i, c)
        cv.set(int(bx + lean), by - height + 1, tip)
    cv.outline(edge)
    for _ in range(4):
        x, y = rnd.randrange(3, 13), rnd.randrange(5, 14)
        if cv.filled(x, y) and cv.get(x, y) != edge:
            cv.set(x, y, tip)
    cv.save(path)

def void_plasma():
    frames = 16
    sheet = Canvas(16, 16 * frames)
    for f in range(frames):
        ph = f / frames * math.tau
        for y in range(16):
            for x in range(16):
                v = (math.sin((x / 16) * math.tau * 2 + ph) + math.sin((y / 16) * math.tau + ph * 2)
                     + math.sin(((x + y) / 16) * math.tau + ph)) / 3
                c = pick(PLASMA, 0.45 + v * 0.45)
                sheet.set(x, f * 16 + y, (c[0], c[1], c[2], 215))
        for k in range(3):
            bx = int((k * 5 + f * 0.6) % 16)
            by = int((k * 7 + f) % 16)
            sheet.set(bx, f * 16 + by, (*PLASMA[5][:3], 235))
    sheet.save("block/void_plasma.png")
    (TEX / "block/void_plasma.png.mcmeta").write_text(json.dumps({"animation": {"frametime": 3, "interpolate": True}}, indent=2) + "\n")


def updraft_vent():
    top = noise_block(VITREOUS, 61, blotch=0.4)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 2.2:
                top.set(x, y, ENERGY[3] if d < 1 else ENERGY[2])
            elif d < 3.6:
                top.set(x, y, ENERGY[0] if (x + y) % 2 else VITREOUS_SHINE[1])
            elif d < 4.6:
                top.set(x, y, VITREOUS_SHINE[2] if y < 8 else VITREOUS[0])
            elif d < 6.8 and (int(math.degrees(math.atan2(y - 7.5, x - 7.5))) // 45) % 2 == 0 and d > 5.4:
                top.set(x, y, VITREOUS_SHINE[0])
    top.save("block/updraft_vent_top.png")

    side = noise_block(VITREOUS, 62, blotch=0.45)
    for x in (3, 7, 8, 12):
        for y in range(2, 14):
            side.set(x, y, ENERGY[0] if y % 4 else ENERGY[1])
        side.set(x, 1, VITREOUS_SHINE[2])
        side.set(x, 14, VITREOUS[0])
    side.save("block/updraft_vent_side.png")


def resonant_spire():
    rnd = random.Random(71)
    side = Canvas()
    for x in range(16):
        base = [2, 3, 4, 3, 2, 3, 4, 5, 4, 3, 2, 3, 4, 3, 2, 1][x]
        for y in range(16):
            side.set(x, y, QUARTZ[base])
    for x in (0, 5, 10, 15):
        for y in range(16):
            side.set(x, y, QUARTZ[1])
    for _ in range(6):  # brilhos rosados/ciano (iridescência)
        x, y = rnd.randrange(16), rnd.randrange(16)
        side.setw(x, y, QUARTZ_TINT[rnd.randrange(2)])
    for y in (0, 15):
        for x in range(16):
            side.set(x, y, QUARTZ[2])
    side.save("block/resonant_spire.png")

    top = Canvas()
    for y in range(16):
        for x in range(16):
            # faces hexagonais
            q = (x - 7.5) / 1.0
            r = (y - 7.5) / 1.15
            d = max(abs(q), abs(r) + abs(q) / 2)
            top.set(x, y, QUARTZ[5 - min(5, int(d / 1.6))])
    for i in range(16):
        top.set(i, 0, QUARTZ[1]); top.set(i, 15, QUARTZ[1]); top.set(0, i, QUARTZ[1]); top.set(15, i, QUARTZ[1])
    top.set(7, 7, QUARTZ_TINT[1]); top.set(8, 8, QUARTZ_TINT[0])
    top.save("block/resonant_spire_top.png")


def void_mud():
    rnd = random.Random(81)
    cv = noise_block(MUD, 82, blotch=0.5)
    for _ in range(5):  # brilho úmido
        x, y = rnd.randrange(16), rnd.randrange(16)
        cv.setw(x, y, MUD[5]); cv.setw(x + 1, y, MUD[4])
    for _ in range(3):
        cv.setw(rnd.randrange(16), rnd.randrange(16), hexc("#6b5fa3"))
    cv.save("block/void_mud.png")


def fen_bricks(cracked: bool):
    rnd = random.Random(91 if not cracked else 92)
    cv = Canvas()
    for row in range(4):
        offset = 0 if row % 2 == 0 else 4
        for y in range(row * 4, row * 4 + 4):
            for x in range(16):
                in_mortar = (y == row * 4 + 3) or ((x + offset) % 8 == 7)
                if in_mortar:
                    cv.set(x, y, FEN_MORTAR)
                else:
                    edge_top = y == row * 4
                    edge_left = (x + offset) % 8 == 0
                    tone = 4 if edge_top or edge_left else rnd.choice([2, 2, 3, 3, 1])
                    cv.set(x, y, FEN[tone])
    for _ in range(8):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if cv.get(x, y) != FEN_MORTAR:
            cv.set(x, y, hexc("#3d5a5a"))  # musgo-do-vazio esverdeado
    if cracked:
        x, y = 3, 0
        for _ in range(14):
            cv.set(x, y, FEN_MORTAR)
            cv.set(x + 1, y, FEN[0])
            x += rnd.choice([-1, 0, 1, 1])
            y += 1
        for i in range(6):
            cv.set(10 + i // 2, 9 + i, FEN_MORTAR)
    cv.save("block/cracked_fen_bricks.png" if cracked else "block/fen_bricks.png")


def glowfen_lily():
    cv = Canvas()
    pad = [(x, y) for x, y in disc(7.5, 7.5, 7.0) if not (x >= 8 and y <= 7 and abs((x - 7.5) - (7.5 - y)) < 1.2)]
    shade_shape(pad, LILY[1:], seed=7, cv=cv)
    cv.outline(LILY[0])
    for a in range(0, 360, 45):
        r = math.radians(a + 22)
        for d in range(2, 6):
            x, y = int(7.5 + math.cos(r) * d), int(7.5 + math.sin(r) * d)
            if cv.filled(x, y) and cv.get(x, y) != LILY[0]:
                cv.set(x, y, LILY[2])
    for x, y in disc(7.5, 7.5, 1.6):
        cv.set(x, y, CYAN[2])
    cv.set(7, 7, hexc("#ffffff"))
    cv.save("block/glowfen_lily.png")

def voidreed():
    rnd = random.Random(101)
    cv = Canvas()
    for bx, h, lean in [(3, 9, -1), (6, 14, 0), (9, 12, 1), (12, 8, 1)]:
        for i in range(h):
            y = 15 - i
            x = bx + int(lean * (i / h) * 2)
            cv.set(x, y, REED[2] if i % 4 else REED[1])
            cv.set(x + 1, y, REED[0]) if i < h - 3 else None
            if i == h - 3:  # folha lateral
                cv.set(x - 1, y, REED[3]); cv.set(x - 2, y - 1, REED[3])
        top_x = bx + int(lean * 2)
        cv.set(top_x, 15 - h, ENERGY[1]); cv.set(top_x, 14 - h, ENERGY[3])
        cv.set(top_x + 1, 15 - h, ENERGY[0])
    cv.save("block/voidreed.png")

def void_anchor():
    cv = Canvas()
    shape = set()
    ring = [(x, y) for x, y in disc(8, 3.5, 3.2)]
    shape |= set(ring)
    for y in range(6, 14):
        shape |= {(7, y), (8, y)}
    for x in range(4, 12):
        shape |= {(x, 7), (x, 8)} if x in (4, 11) else {(x, 7)}
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 6.5)
            if 6.0 <= d <= 7.9 and y >= 9:
                shape.add((x, y))  # braços curvos e grossos
    for x, y in [(0, 9), (1, 8), (1, 9), (15, 9), (14, 8), (14, 9)]:
        shape.add((x, y))  # unhas
    shade_shape(sorted(shape), ANCHOR[2:], seed=3, cv=cv)
    cv.outline(ANCHOR[0])
    for x, y in disc(8, 3.5, 1.6):
        cv.set(x, y, ENERGY[2])
    cv.set(7, 3, ENERGY[3]); cv.set(8, 4, ENERGY[1])
    for y in range(8, 13):
        cv.set(7 + (y % 2), y, ENERGY[0])
    cv.save("item/void_anchor.png")

def silk_glider(broken: bool):
    """Duas asas de seda lado a lado, separadas por 2 px, com pontas franjadas para baixo."""
    rnd = random.Random(111 if not broken else 112)
    cv = Canvas()
    pal = SILK_LEAF[2:] if not broken else ramp("#8d8a9e", "#a9a6b8", "#c5c2d2")
    outer = {1: 4, 2: 2}
    for y in range(1, 15):
        edge = outer.get(y, 1 if y <= 8 else 1 + (y - 8))
        for x in range(edge, 7):
            if y >= 12 and (x + y) % 2 == 0:
                continue  # franja
            if broken and y > 6 and x < 5 and rnd.random() < 0.55:
                continue
            c = pick(pal, (1 - y / 16) * 0.75 + (x / 7) * 0.3)
            cv.set(x, y, c)
            cv.set(15 - x, y, pick(pal, (1 - y / 16) * 0.6 + (x / 7) * 0.2))
    cv.outline(SILK_LEAF[0] if not broken else hexc("#5f5c6e"))
    for y in range(3, 12, 2):
        for x in (4, 11):
            if cv.filled(x, y) and cv.get(x, y)[3]:
                cv.set(x, y, SILK_BARK[2])
    if not broken:
        cv.set(3, 3, GLOW); cv.set(12, 3, GLOW); cv.set(5, 2, hexc("#ffffff")); cv.set(10, 2, hexc("#ffffff"))
    cv.save("item/silk_glider_broken.png" if broken else "item/silk_glider.png")

def null_step_boots():
    cv = Canvas()
    for ox in (1, 9):
        boot = [(ox + x, y) for y in range(4, 13) for x in range(0, 3)] + [(ox + x, y) for y in range(10, 14) for x in range(0, 6)]
        shade_shape(boot, BOOT[1:], seed=ox, cv=cv)
    cv.outline(BOOT[0])
    for ox in (1, 9):
        for x in range(ox + 1, ox + 5):  # sola antigravidade
            cv.set(x, 13, CYAN[2])
        cv.set(ox, 13, CYAN[1]); cv.set(ox + 5, 13, CYAN[1])
        cv.set(ox + 1, 7, CYAN[3]); cv.set(ox + 1, 8, CYAN[1])  # anel de fase
        cv.set(ox + 1, 4, BOOT[4])
    cv.save("item/null_step_boots.png")


def resonance_compass():
    for k in range(32):
        cv = Canvas()
        rim = disc(7.5, 7.5, 7.2)
        shade_shape(rim, QUARTZ[1:], seed=k % 3, cv=cv)
        cv.outline(QUARTZ[0])
        for x, y in disc(7.5, 7.5, 5.2):
            cv.set(x, y, hexc("#1d1833") if (x + y) % 5 else hexc("#241d40"))
        for a in range(0, 360, 90):  # marcas
            r = math.radians(a)
            cv.set(int(7.5 + math.cos(r) * 4.5), int(7.5 + math.sin(r) * 4.5), hexc("#5a4790"))
        theta = k / 32 * math.tau
        dx, dy = -math.sin(theta), math.cos(theta)
        for i in range(0, 5):  # agulha: ponta cristalina rosa, cauda escura
            cv.set(int(round(7.5 + dx * i - 0.5)), int(round(7.5 + dy * i - 0.5)), QUARTZ_TINT[0] if i > 1 else hexc("#ffffff"))
        for i in range(1, 4):
            cv.set(int(round(7.5 - dx * i - 0.5)), int(round(7.5 - dy * i - 0.5)), hexc("#6b5fa3"))
        cv.set(7, 7, QUARTZ_TINT[1])
        cv.save(f"item/resonance_compass_{k:02d}.png")


def diagonal_tool(path, head_pixels, head_pal, edge, handle_pal, handle_len=8, accents=()):
    cv = Canvas()
    for i in range(handle_len):  # cabo de baixo-esquerda para cima-direita
        x, y = 2 + i, 13 - i
        cv.set(x, y, handle_pal[2 if i % 3 else 3])
        cv.set(x - 1, y, handle_pal[1])
    shade_shape(head_pixels, head_pal, seed=len(path), cv=cv)
    cv.outline(edge, diagonal=False)
    for x, y, c in accents:
        cv.set(x, y, c)
    cv.save(path)


def vitreous_cleaver():
    """Cutelo: lâmina larga de vidro negro com o dorso pesado embaixo, guarda curta e cabo claro."""
    blade, guard = [], []
    gx, gy = 5.0, 10.0  # guarda
    for y in range(16):
        for x in range(16):
            rx, ry = x + 0.5 - gx, y + 0.5 - gy
            along = (rx - ry) / 1.4142   # para a ponta (cima-direita)
            cross = (rx + ry) / 1.4142   # para o dorso (baixo-direita)
            if 0.8 <= along <= 13.2:
                top = 1.9 if along < 10 else 1.9 - (along - 10) * 0.9
                if -1.1 <= cross <= top:
                    blade.append((x, y))
            elif -0.6 <= along <= 0.8 and abs(cross) <= 2.9:
                guard.append((x, y))
    diagonal_tool("item/vitreous_cleaver.png", sorted(set(blade) | set(guard)), VITREOUS + VITREOUS_SHINE[:2], VITREOUS[0], END_ROD,
                  handle_len=4, accents=[(6 + i, 8 - i, VITREOUS_SHINE[2]) for i in range(7)] + [(13, 1, VITREOUS_SHINE[3]), (12, 2, ENERGY[1])])

def hollow_core_pickaxe():
    """Cabeça em arco concêntrica ao cabo, como as picaretas do jogo."""
    head = []
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 2.5, y - 13.5)
            if 10.2 <= d <= 12.4 and x >= 3 and y <= 12 and x + (15 - y) >= 13:
                head.append((x, y))
    diagonal_tool("item/hollow_core_pickaxe.png", head, ANCHOR[2:], ANCHOR[0], END_ROD, handle_len=9,
                  accents=[(10, 5, ENERGY[3]), (11, 4, ENERGY[1]), (10, 4, ENERGY[2]), (11, 5, ENERGY[0]),
                           (4, 2, ENERGY[0]), (13, 11, ENERGY[0])])

def echo_bow(stage: int | None):
    """Arco: um único arco curvado para cima-esquerda, pontas em cima-direita e baixo-esquerda."""
    cv = Canvas()
    pull = 0 if stage is None else stage + 1
    tip_a, tip_b, bulge = (13.0, 2.0), (2.0, 13.0), (0.5, 0.5)
    pts = []
    for i in range(41):
        t = i / 40
        x = (1 - t) ** 2 * tip_a[0] + 2 * (1 - t) * t * bulge[0] + t * t * tip_b[0]
        y = (1 - t) ** 2 * tip_a[1] + 2 * (1 - t) * t * bulge[1] + t * t * tip_b[1]
        pts.append((int(round(x)), int(round(y))))
    limb = set(pts)
    for x, y in list(limb):  # braços com 2 px de espessura perto da empunhadura
        if 4 <= x + y <= 10:
            limb.add((x + 1, y + 1))
    for x, y in limb:
        cv.set(x, y, END_ROD[3] if x + y < 9 else END_ROD[2])
    cv.outline(END_ROD[0])
    for x, y in [(4, 4), (5, 5), (4, 5), (5, 4)]:
        cv.set(x, y, PLASMA[3])  # empunhadura de púrpura
    cv.set(13, 1, PLASMA[4]); cv.set(1, 13, PLASMA[4])
    bend = pull * 1.4
    mx, my = 7.5 + bend, 7.5 + bend
    cv.line(13, 2, mx, my, ECHO[3])
    cv.line(mx, my, 2, 13, ECHO[3])
    if pull:
        length = 5 + pull * 2
        for i in range(length):  # flecha encaixada apontando para cima-esquerda
            c = hexc("#d8d8d8") if i >= length - 2 else hexc("#8a7a63")
            cv.set(mx - i, my - i, c)
        cv.set(mx - length, my - length + 1, hexc("#a8a8a8")); cv.set(mx - length + 1, my - length, hexc("#a8a8a8"))
        cv.set(mx + 1, my, ECHO[4]); cv.set(mx, my + 1, ECHO[4])
    name = "item/echo_string_bow.png" if stage is None else f"item/echo_string_bow_pulling_{stage}.png"
    cv.save(name)

def simple_items():
    # Fio de Seda Pálida: novelo com ponta solta
    cv = Canvas()
    ball = disc(7, 9, 4.2)
    shade_shape(ball, SILK_LEAF[2:], seed=9, cv=cv)
    for k in range(-3, 4):
        cv.line(4 + k, 6, 10 + k, 12, SILK_LEAF[2]) if k % 2 == 0 else None
    pts = [(10, 6), (11, 4), (13, 3), (14, 2)]
    for i in range(len(pts) - 1):
        cv.line(*pts[i], *pts[i + 1], SILK_LEAF[4])
    cv.outline(SILK_LEAF[0])
    cv.set(14, 2, GLOW); cv.set(6, 7, hexc("#ffffff"))
    cv.save("item/pale_silk_thread.png")

    # Fragmento Ressonante: prisma pontudo nas duas pontas
    def prism(cx0, cy0, cx1, cy1, width):
        pts = set()
        for y in range(16):
            for x in range(16):
                t = ((x - cx0) * (cx1 - cx0) + (y - cy0) * (cy1 - cy0)) / ((cx1 - cx0) ** 2 + (cy1 - cy0) ** 2)
                if 0 <= t <= 1:
                    px, py = cx0 + (cx1 - cx0) * t, cy0 + (cy1 - cy0) * t
                    w = width * min(1.0, min(t, 1 - t) * 3.2)
                    if math.hypot(x - px, y - py) <= w:
                        pts.add((x, y))
        return sorted(pts)

    cv = Canvas()
    shard = prism(3, 13, 13, 2, 2.4)
    shade_shape(shard, QUARTZ[1:], seed=5, cv=cv)
    cv.outline(QUARTZ[0])
    for i in range(5):
        cv.set(6 + i, 10 - i, QUARTZ[5] if i % 2 else QUARTZ_TINT[i % 2])
    cv.save("item/resonant_shard.png")

    # Fragmento Vítreo: lasca irregular, mais larga numa ponta
    cv = Canvas()
    shard = set(prism(2, 14, 13, 2, 2.0)) | set(prism(7, 9, 13, 5, 1.6))
    shade_shape(sorted(shard), VITREOUS[1:] + VITREOUS_SHINE[:1], seed=6, cv=cv)
    cv.outline(VITREOUS[0])
    for i in range(5):
        cv.set(5 + i, 10 - i, VITREOUS_SHINE[2 if i % 2 else 1])
    cv.set(12, 3, VITREOUS_SHINE[3])
    cv.save("item/vitreous_shard.png")

    # Corda de Eco: meada enrolada em oito
    cv = Canvas()
    for cx, cy in [(5.5, 8), (10.5, 8)]:
        for y in range(16):
            for x in range(16):
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if 2.0 <= d <= 3.6:
                    cv.set(x, y, ECHO[3] if y < cy else ECHO[2])
    cv.outline(ECHO[0])
    cv.set(4, 5, ECHO[4]); cv.set(11, 5, ECHO[4]); cv.set(8, 8, ECHO[4])
    cv.save("item/echo_string.png")

    # Núcleo Oco
    cv = Canvas()
    shell = [(x, y) for x, y in disc(7.5, 7.5, 6.6)]
    shade_shape(shell, ANCHOR[1:], seed=8, cv=cv)
    cv.outline(ANCHOR[0])
    for x, y in disc(7.5, 7.5, 3.4):
        cv.set(x, y, ENERGY[0])
    for x, y in disc(7.5, 7.5, 2.0):
        cv.set(x, y, hexc("#0b0414"))
    cv.set(5, 5, ENERGY[3]); cv.set(4, 4, ANCHOR[5])
    cv.save("item/hollow_core.png")

def null_step_armor_layer():
    cv = Canvas(64, 32)
    # Perna do modelo humanoide: caixa 4x12x4 em (0,16). Botas cobrem a metade de baixo.
    rnd = random.Random(121)
    for face_x in (0, 4, 8, 12):
        for x in range(face_x, face_x + 4):
            for y in range(26, 32):
                cv.set(x, y, BOOT[rnd.choice([1, 2, 2, 3])])
            cv.set(x, 31, CYAN[2] if face_x in (4, 12) else CYAN[1])
            cv.set(x, 26, BOOT[4])
        cv.set(face_x + 1, 28, CYAN[3])
    for x in range(8, 12):  # sola (face de baixo)
        for y in range(16, 20):
            cv.set(x, y, CYAN[1] if (x + y) % 2 else CYAN[0])
    cv.save("entity/equipment/humanoid/null_step.png")


def silk_glider_wings():
    cv = Canvas(64, 32)
    rnd = random.Random(131)
    # Caixa da asa 10x20x2 em (22,0): faces frente (24..34) e trás (36..46) com 20 px de altura.
    for face_x in (24, 36):
        for x in range(10):
            for y in range(2, 22):
                dist = (x / 9.0) if face_x == 24 else (1 - x / 9.0)
                if y > 2 + 18 * (1 - dist * 0.7) + rnd.random() * 2:
                    continue  # borda recortada como membrana de seda
                c = SILK_LEAF[2 + int((1 - y / 22) * 3)]
                cv.set(face_x + x, y, c)
            for y in range(4, 20, 4):
                cv.set(face_x + x, y, SILK_BARK[3])  # costuras
        for y in range(2, 22, 5):
            cv.set(face_x + (2 if face_x == 24 else 7), y, GLOW)
    for x in range(22, 24):
        for y in range(2, 22):
            cv.set(x, y, SILK_BARK[3])
    for x in range(34, 36):
        for y in range(2, 22):
            cv.set(x, y, SILK_BARK[3])
    for x in range(24, 44):
        for y in range(0, 2):
            cv.set(x, y, SILK_BARK[4])
    cv.save("entity/equipment/wings/silk_glider.png")


def particles():
    for i, (core, halo) in enumerate([(hexc("#ffffff"), hexc("#d9fbff", 120)), (hexc("#f0e8ff"), hexc("#c9b8f5", 110))]):
        cv = Canvas(8, 8)
        for y in range(8):
            for x in range(8):
                d = math.hypot(x - 3.5, y - 3.5)
                if d < 1.2:
                    cv.set(x, y, core)
                elif d < 2.6:
                    cv.set(x, y, halo)
        cv.save(f"particle/silk_spore_{i}.png")


def icon():
    size = 128
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = im.load()
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - 63.5, y - 63.5) / 64
            if d <= 1.0:
                t = y / size
                r = int(20 + 40 * (1 - t)); g = int(12 + 20 * (1 - t)); b = int(38 + 70 * (1 - t))
                px[x, y] = (r, g, b, 255)
    rnd = random.Random(141)
    for _ in range(60):
        x, y = rnd.randrange(size), rnd.randrange(size)
        if px[x, y][3]:
            px[x, y] = (230, 220, 255, 255)
    anchor = Image.open(TEX / "item/void_anchor.png").resize((96, 96), Image.NEAREST)
    im.alpha_composite(anchor, (16, 14))
    out = TEX.parent / "icon.png"
    im.save(out)


def main():
    pale_silk_log(); pale_silk_planks(); pale_silk_leaves(); pale_silk_sapling(); luminous_silk(); pale_silk_moss()
    vitreous_obsidian(); void_plasma(); updraft_vent()
    crystal_cluster("block/vitreous_shard_cluster.png", VITREOUS + VITREOUS_SHINE[:1], VITREOUS[0], VITREOUS_SHINE[3], 1)
    crystal_cluster("block/resonant_cluster.png", QUARTZ, QUARTZ[0], hexc("#ffffff"), 2)
    resonant_spire(); void_mud(); fen_bricks(False); fen_bricks(True); glowfen_lily(); voidreed()

    void_anchor(); silk_glider(False); silk_glider(True); null_step_boots(); resonance_compass()
    vitreous_cleaver(); hollow_core_pickaxe()
    echo_bow(None)
    for s in range(3):
        echo_bow(s)
    simple_items()

    null_step_armor_layer(); silk_glider_wings(); particles(); icon()
    count = sum(1 for _ in TEX.rglob("*.png"))
    print(f"{count} texturas geradas em {TEX.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
