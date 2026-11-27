#!/usr/bin/env python3
"""
Renderiza uma vista de cima de um trecho do End gerado pelo jogo (lendo os arquivos .mca).

Uso: python3 tools/render_end_map.py <pasta-do-mundo> <x0> <z0> <x1> <z1> <saida.png>
Cores dos blocos do Hollow Horizons vêm da média das próprias texturas.
"""
from __future__ import annotations

import io
import struct
import sys
import zlib
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/hollowhorizons/textures/block"

VANILLA = {
    "minecraft:end_stone": (219, 222, 158), "minecraft:chorus_plant": (94, 58, 94), "minecraft:chorus_flower": (150, 110, 150),
    "minecraft:obsidian": (20, 16, 30), "minecraft:bedrock": (80, 80, 80), "minecraft:end_gateway": (10, 10, 20),
    "minecraft:water": (27, 20, 48), "minecraft:purpur_block": (170, 125, 170), "minecraft:purpur_pillar": (172, 130, 172),
    "minecraft:purpur_stairs": (165, 120, 165), "minecraft:purpur_slab": (165, 120, 165), "minecraft:end_stone_bricks": (220, 224, 160),
    "minecraft:end_rod": (240, 235, 230), "minecraft:magenta_stained_glass": (178, 76, 216), "minecraft:chest": (160, 110, 40),
}
OWN_FILES = {"pale_silk_moss": "pale_silk_moss_top", "pale_silk_log": "pale_silk_log_top", "updraft_vent": "updraft_vent_top",
             "resonant_spire": "resonant_spire_top"}


def own_color(name: str):
    path = TEX / (OWN_FILES.get(name, name) + ".png")
    if not path.exists():
        return (255, 0, 255)
    im = Image.open(path).convert("RGBA").crop((0, 0, 16, 16))
    px = [im.getpixel((x, y)) for y in range(16) for x in range(16) if im.getpixel((x, y))[3] > 0]
    return tuple(sum(c[i] for c in px) // len(px) for i in range(3))


def nbt(buf: io.BytesIO, tag: int):
    r = lambda fmt: struct.unpack(">" + fmt, buf.read(struct.calcsize(fmt)))[0]
    if tag == 1: return r("b")
    if tag == 2: return r("h")
    if tag == 3: return r("i")
    if tag == 4: return r("q")
    if tag == 5: return r("f")
    if tag == 6: return r("d")
    if tag == 7: return buf.read(r("i"))
    if tag == 8: return buf.read(r("H")).decode("utf-8", "replace")
    if tag == 9:
        inner, n = r("b"), r("i")
        return [nbt(buf, inner) for _ in range(n)]
    if tag == 10:
        out = {}
        while True:
            t = r("b")
            if t == 0:
                return out
            name = buf.read(r("H")).decode("utf-8", "replace")
            out[name] = nbt(buf, t)
    if tag == 11: return [r("i") for _ in range(r("i"))]
    if tag == 12: return [r("q") for _ in range(r("i"))]
    raise ValueError(tag)


def block_name(entry) -> str:
    """Paleta de 26.x: string, {"id": ..., "properties": ...} ou {"": valor} (lista heterogênea de NBT)."""
    if isinstance(entry, str):
        return entry
    if "" in entry:
        return block_name(entry[""])
    return entry.get("id") or entry.get("Name") or "?"


def chunk_columns(chunk):
    """Devolve {(x,z): (y, nome)} do bloco mais alto não-ar de cada coluna."""
    top = {}
    sections = sorted(chunk.get("sections", []), key=lambda s: s["Y"], reverse=True)
    for sec in sections:
        bs = sec.get("block_states")
        if not bs:
            continue
        palette = [block_name(p) for p in bs["palette"]]
        if palette == ["minecraft:air"]:
            continue
        data = bs.get("data")
        bits = max(4, (len(palette) - 1).bit_length())
        per_long = 64 // bits
        mask = (1 << bits) - 1
        for ly in range(15, -1, -1):
            for lz in range(16):
                for lx in range(16):
                    if (lx, lz) in top:
                        continue
                    i = ly * 256 + lz * 16 + lx
                    if data is None:
                        name = palette[0]
                    else:
                        word = data[i // per_long] & 0xFFFFFFFFFFFFFFFF
                        name = palette[(word >> ((i % per_long) * bits)) & mask]
                    if name not in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air"):
                        top[(lx, lz)] = (sec["Y"] * 16 + ly, name)
        if len(top) == 256:
            break
    return top


def main():
    world, x0, z0, x1, z1, out = sys.argv[1], *map(int, sys.argv[2:6]), sys.argv[6]
    region_dir = Path(world) / "dimensions/minecraft/the_end/region"
    if not region_dir.exists():
        region_dir = Path(world) / "DIM1/region"
    w, h = x1 - x0, z1 - z0
    img = Image.new("RGB", (w, h), (6, 4, 12))
    colors = {}
    for cz in range(z0 >> 4, (z1 >> 4) + 1):
        for cx in range(x0 >> 4, (x1 >> 4) + 1):
            region = region_dir / f"r.{cx >> 5}.{cz >> 5}.mca"
            if not region.exists():
                continue
            with open(region, "rb") as f:
                f.seek(4 * ((cx & 31) + (cz & 31) * 32))
                loc = struct.unpack(">I", f.read(4))[0]
                if loc == 0:
                    continue
                f.seek((loc >> 8) * 4096)
                length, comp = struct.unpack(">IB", f.read(5))
                raw = f.read(length - 1)
            data = zlib.decompress(raw) if comp == 2 else raw
            buf = io.BytesIO(data)
            buf.read(1); buf.read(struct.unpack(">H", buf.read(2))[0])
            chunk = nbt(buf, 10)
            if chunk.get("Status", "") not in ("minecraft:full", "full"):
                continue
            for (lx, lz), (y, name) in chunk_columns(chunk).items():
                px, pz = cx * 16 + lx - x0, cz * 16 + lz - z0
                if 0 <= px < w and 0 <= pz < h:
                    if name not in colors:
                        colors[name] = own_color(name.split(":")[1]) if name.startswith("hollowhorizons:") else VANILLA.get(name, (128, 128, 128))
                    c = colors[name]
                    shade = max(0.55, min(1.25, 0.55 + (y - 20) / 90))
                    img.putpixel((px, pz), tuple(min(255, int(v * shade)) for v in c))
    img = img.resize((w * 3, h * 3), Image.NEAREST)
    img.save(out)
    print(f"mapa salvo em {out} ({len(colors)} tipos de bloco)")


if __name__ == "__main__":
    main()
