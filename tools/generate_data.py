#!/usr/bin/env python3
"""
Gerador dos recursos JSON de Hollow Horizons (assets + data).

Mantém num só lugar a lista de blocos/itens e escreve: blockstates, modelos, definições de
item, equipamentos, partículas, loot tables, receitas, tags e todo o worldgen dos quatro
biomas novos do End. Só usa a biblioteca padrão do Python.

Uso:  python3 tools/generate_data.py
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/hollowhorizons"
DATA = RES / "data"
NS = "hollowhorizons"


def ns(path: str) -> str:
    return path if ":" in path else f"{NS}:{path}"


def write(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


# =========================================================================== catálogo

# (id, tipo de modelo)  tipos: cube, cube_bottom_top, pillar, leaves, cross, cluster, lily, translucent_cube
BLOCKS = [
    ("pale_silk_log", "pillar"),
    ("pale_silk_planks", "cube"),
    ("pale_silk_leaves", "leaves"),
    ("pale_silk_sapling", "cross"),
    ("luminous_silk", "cross"),
    ("pale_silk_moss", "moss"),
    ("vitreous_obsidian", "cube"),
    ("vitreous_shard_cluster", "cluster"),
    ("void_plasma", "cube"),
    ("updraft_vent", "vent"),
    ("resonant_spire", "pillar"),
    ("resonant_cluster", "cluster"),
    ("void_mud", "cube"),
    ("glowfen_lily", "lily"),
    ("voidreed", "cross"),
    ("fen_bricks", "cube"),
    ("cracked_fen_bricks", "cube"),
]
BLOCKS_WITHOUT_ITEM = {"void_plasma"}
FLAT_BLOCK_ITEMS = {"pale_silk_sapling", "luminous_silk", "voidreed", "vitreous_shard_cluster", "resonant_cluster", "glowfen_lily"}

SIMPLE_ITEMS = ["pale_silk_thread", "resonant_shard", "vitreous_shard", "echo_string", "hollow_core", "void_anchor", "null_step_boots"]
HANDHELD_ITEMS = ["vitreous_cleaver", "hollow_core_pickaxe"]

LANG = {
    # ---------------------------------------------------------------- blocos
    "block.hollowhorizons.pale_silk_log": ("Pale Silk Log", "Tronco de Seda Pálida"),
    "block.hollowhorizons.pale_silk_planks": ("Pale Silk Planks", "Tábuas de Seda Pálida"),
    "block.hollowhorizons.pale_silk_leaves": ("Pale Silk Leaves", "Folhas de Seda Pálida"),
    "block.hollowhorizons.pale_silk_sapling": ("Pale Silk Sapling", "Muda de Seda Pálida"),
    "block.hollowhorizons.luminous_silk": ("Luminous Silk", "Teia de Luz"),
    "block.hollowhorizons.pale_silk_moss": ("Pale Silk Moss", "Musgo de Seda Pálida"),
    "block.hollowhorizons.vitreous_obsidian": ("Vitreous Obsidian", "Obsidiana Vítrea"),
    "block.hollowhorizons.vitreous_shard_cluster": ("Vitreous Shard Cluster", "Aglomerado Vítreo"),
    "block.hollowhorizons.void_plasma": ("Void Plasma", "Plasma do Vazio"),
    "block.hollowhorizons.updraft_vent": ("Updraft Vent", "Gêiser de Ar"),
    "block.hollowhorizons.resonant_spire": ("Resonant Spire", "Pilar Ressonante"),
    "block.hollowhorizons.resonant_cluster": ("Resonant Cluster", "Aglomerado Ressonante"),
    "block.hollowhorizons.void_mud": ("Void Mud", "Lama do Vazio"),
    "block.hollowhorizons.glowfen_lily": ("Glowfen Lily", "Lírio-do-Pântano"),
    "block.hollowhorizons.voidreed": ("Voidreed", "Junco do Vazio"),
    "block.hollowhorizons.fen_bricks": ("Fen Bricks", "Tijolos do Pântano"),
    "block.hollowhorizons.cracked_fen_bricks": ("Cracked Fen Bricks", "Tijolos do Pântano Rachados"),
    # ---------------------------------------------------------------- itens
    "item.hollowhorizons.pale_silk_thread": ("Pale Silk Thread", "Fio de Seda Pálida"),
    "item.hollowhorizons.resonant_shard": ("Resonant Shard", "Fragmento Ressonante"),
    "item.hollowhorizons.vitreous_shard": ("Vitreous Shard", "Fragmento Vítreo"),
    "item.hollowhorizons.echo_string": ("Echo String", "Corda de Eco"),
    "item.hollowhorizons.hollow_core": ("Hollow Core", "Núcleo Oco"),
    "item.hollowhorizons.void_anchor": ("Void Anchor", "Âncora do Vazio"),
    "item.hollowhorizons.silk_glider": ("Silk Glider", "Asas de Seda"),
    "item.hollowhorizons.null_step_boots": ("Null-Step Boots", "Botas de Gravidade Zero"),
    "item.hollowhorizons.resonance_compass": ("Resonance Compass", "Bússola de Ressonância"),
    "item.hollowhorizons.vitreous_cleaver": ("Vitreous Cleaver", "Lâmina de Obsidiana Vítrea"),
    "item.hollowhorizons.echo_string_bow": ("Echo String Bow", "Arco de Eco"),
    "item.hollowhorizons.hollow_core_pickaxe": ("Hollow-Core Pickaxe", "Picareta de Núcleo Oco"),
    "itemGroup.hollowhorizons": ("Hollow Horizons", "Hollow Horizons"),
    # ---------------------------------------------------------------- biomas
    "biome.hollowhorizons.pale_silk_woods": ("Pale Silk Woods", "Florestas de Seda Pálida"),
    "biome.hollowhorizons.vitreous_chasms": ("Vitreous Chasms", "Desfiladeiros de Obsidiana Vítrea"),
    "biome.hollowhorizons.resonant_spirefields": ("Resonant Spirefields", "Campos de Cristal Ressonante"),
    "biome.hollowhorizons.void_fen": ("Void Fen", "Pântanos do Vazio"),
    # ---------------------------------------------------------------- mensagens e dicas
    "item.hollowhorizons.void_anchor.charges": ("Charges: %s/%s", "Cargas: %s/%s"),
    "item.hollowhorizons.void_anchor.tooltip": (
        "Pulls you back to solid ground if you fall into the Void. Use to recharge with an Ender Pearl; sneak + use to recall.",
        "Puxa você de volta ao chão firme se cair no Vazio. Use para recarregar com Pérola do Ender; agache + use para voltar."),
    "item.hollowhorizons.void_anchor.full": ("The anchor is fully charged", "A âncora está carregada"),
    "item.hollowhorizons.void_anchor.need_pearl": ("Needs an Ender Pearl to recharge", "Precisa de uma Pérola do Ender para recarregar"),
    "item.hollowhorizons.void_anchor.cooldown": ("The anchor's chain is still recharging", "A corrente da âncora ainda está recarregando"),
    "item.hollowhorizons.void_anchor.empty": ("The anchor has no charges", "A âncora está sem cargas"),
    "item.hollowhorizons.void_anchor.saved": ("The Void Anchor pulled you back!", "A Âncora do Vazio puxou você de volta!"),
    "item.hollowhorizons.void_anchor.recalled": ("Anchored back to solid ground", "Ancorado de volta ao chão firme"),
    "item.hollowhorizons.silk_glider.no_rockets": ("Silk Gliders are too fragile for rockets", "As Asas de Seda são frágeis demais para foguetes"),
    "item.hollowhorizons.resonance_compass.target.end_city": ("End City & Ship", "Cidade do End e Navio"),
    "item.hollowhorizons.resonance_compass.target.void_ruins": ("Void Ruins", "Ruínas do Vazio"),
    "item.hollowhorizons.resonance_compass.current": ("Tuned to: %s", "Afinada em: %s"),
    "item.hollowhorizons.resonance_compass.tooltip": (
        "Use to resonate (costs a Resonant Shard). Sneak + use to change target.",
        "Use para ressoar (gasta um Fragmento Ressonante). Agache + use para trocar o alvo."),
    "item.hollowhorizons.resonance_compass.tuned": ("Compass tuned to %s", "Bússola afinada em %s"),
    "item.hollowhorizons.resonance_compass.wrong_dimension": ("The crystals only resonate in the End", "Os cristais só ressoam no End"),
    "item.hollowhorizons.resonance_compass.need_shard": ("Needs a Resonant Shard", "Precisa de um Fragmento Ressonante"),
    "item.hollowhorizons.resonance_compass.silent": ("No %s answers nearby", "Nenhuma %s responde por perto"),
    "item.hollowhorizons.resonance_compass.found": ("%s resonates %s blocks away", "%s ressoa a %s blocos"),
    "item.hollowhorizons.vitreous_cleaver.tooltip.end": ("+%s damage against End creatures", "+%s de dano contra criaturas do End"),
    "item.hollowhorizons.vitreous_cleaver.tooltip.armor": ("Ignores %s%% of armor", "Ignora %s%% da armadura"),
    "item.hollowhorizons.echo_string_bow.tooltip": (
        "Arrows fly straight, ignore gravity and pierce %s enemies",
        "Flechas voam em linha reta, sem gravidade, e atravessam %s inimigos"),
    "item.hollowhorizons.hollow_core_pickaxe.tooltip.instant": ("Breaks End blocks instantly", "Quebra blocos do End instantaneamente"),
    "item.hollowhorizons.hollow_core_pickaxe.tooltip.magnet": ("Mined items go straight to your inventory", "Itens minerados vão direto para o inventário"),
}


# =========================================================================== assets

def blockstate_and_models():
    for block, kind in BLOCKS:
        model = f"{NS}:block/{block}"
        tex = f"{NS}:block/{block}"
        bs = ASSETS / "blockstates" / f"{block}.json"
        mdl = ASSETS / "models/block"
        if kind in ("cube",):
            write(mdl / f"{block}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": tex}})
            write(bs, {"variants": {"": {"model": model}}})
        elif kind == "leaves":
            write(mdl / f"{block}.json", {"parent": "minecraft:block/leaves", "textures": {"all": tex}})
            write(bs, {"variants": {"": {"model": model}}})
        elif kind == "cross":
            write(mdl / f"{block}.json", {"parent": "minecraft:block/cross", "textures": {"cross": tex}})
            write(bs, {"variants": {"": {"model": model}}})
        elif kind == "pillar":
            write(mdl / f"{block}.json", {"parent": "minecraft:block/cube_column", "textures": {"end": tex + "_top", "side": tex}})
            write(mdl / f"{block}_horizontal.json", {"parent": "minecraft:block/cube_column_horizontal", "textures": {"end": tex + "_top", "side": tex}})
            write(bs, {"variants": {
                "axis=x": {"model": model + "_horizontal", "x": 90, "y": 90},
                "axis=y": {"model": model},
                "axis=z": {"model": model + "_horizontal", "x": 90},
            }})
        elif kind == "moss":
            write(mdl / f"{block}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
                "top": tex + "_top", "side": tex + "_side", "bottom": "minecraft:block/end_stone"}})
            write(bs, {"variants": {"": {"model": model}}})
        elif kind == "vent":
            write(mdl / f"{block}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
                "top": tex + "_top", "side": tex + "_side", "bottom": f"{NS}:block/vitreous_obsidian"}})
            write(bs, {"variants": {"": {"model": model}}})
        elif kind == "cluster":
            write(mdl / f"{block}.json", {"parent": "minecraft:block/cross", "textures": {"cross": tex}})
            rot = {"up": {}, "down": {"x": 180}, "north": {"x": 90}, "south": {"x": 90, "y": 180},
                   "east": {"x": 90, "y": 90}, "west": {"x": 90, "y": 270}}
            write(bs, {"variants": {f"facing={f}": {"model": model, **r} for f, r in rot.items()}})
        elif kind == "lily":
            write(mdl / f"{block}.json", {
                "ambientocclusion": False,
                "textures": {"particle": tex, "texture": tex},
                "elements": [{"from": [0, 0.25, 0], "to": [16, 0.25, 16], "faces": {
                    "down": {"uv": [0, 16, 16, 0], "texture": "#texture"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#texture"}}}],
            })
            write(bs, {"variants": {"": {"model": model}}})

        if block in BLOCKS_WITHOUT_ITEM:
            continue
        if block in FLAT_BLOCK_ITEMS:
            write(ASSETS / "models/item" / f"{block}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
            write(ASSETS / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{block}"}})
        else:
            write(ASSETS / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": model}})


def item_models():
    for item in SIMPLE_ITEMS:
        write(ASSETS / "models/item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{item}"}})
        write(ASSETS / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{item}"}})
    for item in HANDHELD_ITEMS:
        write(ASSETS / "models/item" / f"{item}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/{item}"}})
        write(ASSETS / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{item}"}})

    # Asas de Seda (com variante rasgada, como a Elytra)
    for name in ("silk_glider", "silk_glider_broken"):
        write(ASSETS / "models/item" / f"{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    write(ASSETS / "items/silk_glider.json", {"model": {
        "type": "minecraft:condition", "property": "minecraft:broken",
        "on_false": {"type": "minecraft:model", "model": f"{NS}:item/silk_glider"},
        "on_true": {"type": "minecraft:model", "model": f"{NS}:item/silk_glider_broken"}}})

    # Arco de Eco: herda as transformações de exibição do arco vanilla
    for name in ("echo_string_bow", "echo_string_bow_pulling_0", "echo_string_bow_pulling_1", "echo_string_bow_pulling_2"):
        write(ASSETS / "models/item" / f"{name}.json", {"parent": "minecraft:item/bow", "textures": {"layer0": f"{NS}:item/{name}"}})
    write(ASSETS / "items/echo_string_bow.json", {"model": {
        "type": "minecraft:condition", "property": "minecraft:using_item",
        "on_false": {"type": "minecraft:model", "model": f"{NS}:item/echo_string_bow"},
        "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": 0.05,
                    "fallback": {"type": "minecraft:model", "model": f"{NS}:item/echo_string_bow_pulling_0"},
                    "entries": [
                        {"threshold": 0.65, "model": {"type": "minecraft:model", "model": f"{NS}:item/echo_string_bow_pulling_1"}},
                        {"threshold": 0.9, "model": {"type": "minecraft:model", "model": f"{NS}:item/echo_string_bow_pulling_2"}}]}}})

    # Bússola de Ressonância: 32 quadros, mesma convenção de ângulo da bússola vanilla
    entries = []
    for i in range(33):
        frame = (16 + i) % 32
        entries.append({"threshold": 0.0 if i == 0 else i - 0.5,
                        "model": {"type": "minecraft:model", "model": f"{NS}:item/resonance_compass_{frame:02d}"}})
    for frame in range(32):
        write(ASSETS / "models/item" / f"resonance_compass_{frame:02d}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/resonance_compass_{frame:02d}"}})
    write(ASSETS / "items/resonance_compass.json", {"model": {
        "type": "minecraft:condition", "component": "minecraft:lodestone_tracker", "property": "minecraft:has_component",
        "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:compass", "target": "lodestone", "scale": 32.0, "entries": entries},
        "on_false": {"type": "minecraft:range_dispatch", "property": "minecraft:compass", "target": "none", "scale": 32.0, "entries": entries}}})


def equipment_particles_lang():
    write(ASSETS / "equipment/null_step.json", {"layers": {"humanoid": [{"texture": f"{NS}:null_step"}]}})
    write(ASSETS / "equipment/silk_glider.json", {"layers": {"wings": [{"texture": f"{NS}:silk_glider"}]}})
    write(ASSETS / "particles/silk_spore.json", {"textures": [f"{NS}:silk_spore_0", f"{NS}:silk_spore_1"]})
    write(ASSETS / "lang/en_us.json", {k: v[0] for k, v in sorted(LANG.items())})
    write(ASSETS / "lang/pt_br.json", {k: v[1] for k, v in sorted(LANG.items())})


# =========================================================================== data: loot

def self_drop(block):
    return {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{block}", "pools": [
        {"rolls": 1, "condition": {"type": "minecraft:survives_explosion"},
         "entries": [{"type": "minecraft:item", "name": ns(block)}]}]}


SILK_OR_SHEAR = {"type": "minecraft:any_of", "terms": ["minecraft:tool/can_shear", "minecraft:tool/can_silk_touch"]}


def loot_tables():
    root = DATA / NS / "loot_table/blocks"
    for block in ["pale_silk_log", "pale_silk_planks", "pale_silk_sapling", "pale_silk_moss", "vitreous_obsidian", "updraft_vent",
                  "resonant_spire", "void_mud", "glowfen_lily", "fen_bricks", "cracked_fen_bricks"]:
        write(root / f"{block}.json", self_drop(block))

    write(root / "pale_silk_leaves.json", {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/pale_silk_leaves", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "condition": SILK_OR_SHEAR, "name": ns("pale_silk_leaves")},
            {"type": "minecraft:item", "name": ns("pale_silk_sapling"), "condition": {"type": "minecraft:all_of", "terms": [
                {"type": "minecraft:survives_explosion"},
                {"type": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.05, 0.0625, 0.083333336, 0.1]}]}}]}]},
        {"rolls": 1, "condition": {"type": "minecraft:inverted", "term": SILK_OR_SHEAR}, "entries": [
            {"type": "minecraft:item", "name": ns("pale_silk_thread"),
             "condition": {"type": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.1, 0.12, 0.15, 0.2, 0.3]},
             "modifier": [{"type": "minecraft:explosion_decay"}]}]}]})

    write(root / "luminous_silk.json", {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/luminous_silk", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "condition": SILK_OR_SHEAR, "name": ns("luminous_silk")},
            {"type": "minecraft:item", "condition": {"type": "minecraft:survives_explosion"}, "name": ns("pale_silk_thread"),
             "modifier": [{"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]}]})

    write(root / "voidreed.json", {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/voidreed", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "condition": SILK_OR_SHEAR, "name": ns("voidreed")}]}]})

    for cluster, shard, count in [("vitreous_shard_cluster", "vitreous_shard", 3), ("resonant_cluster", "resonant_shard", 4)]:
        write(root / f"{cluster}.json", {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{cluster}", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": ns(cluster)},
                {"type": "minecraft:item", "name": ns(shard),
                 "condition": {"type": "minecraft:match_tool", "predicate": {"items": "#minecraft:cluster_max_harvestables"}},
                 "modifier": [{"type": "minecraft:set_count", "count": count},
                              {"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"}]},
                {"type": "minecraft:item", "name": ns(shard),
                 "modifier": [{"type": "minecraft:set_count", "count": 1}, {"type": "minecraft:explosion_decay"}]}]}]}]})

    def entry(item, weight, lo=1, hi=1, extra=None):
        e = {"type": "minecraft:item", "name": ns(item), "weight": weight}
        mods = []
        if hi > 1:
            mods.append({"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
        if extra:
            mods.append(extra)
        if mods:
            e["modifier"] = mods
        return e

    write(DATA / NS / "loot_table/chests/void_ruins.json", {"type": "minecraft:chest", "random_sequence": f"{NS}:chests/void_ruins", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            entry("minecraft:ender_pearl", 20, 1, 4),
            entry("resonant_shard", 18, 2, 6),
            entry("vitreous_shard", 14, 1, 4),
            entry("pale_silk_thread", 14, 2, 6),
            entry("minecraft:chorus_fruit", 12, 2, 6),
            entry("minecraft:experience_bottle", 8, 1, 3),
            entry("minecraft:diamond", 5, 1, 2),
            entry("echo_string", 5),
            entry("minecraft:echo_shard", 3),
        ]},
        {"rolls": 1, "entries": [
            {"type": "minecraft:empty", "weight": 6},
            entry("hollow_core", 2),
            entry("void_anchor", 2),
            entry("resonance_compass", 2),
            entry("minecraft:diamond_sword", 1, extra={"type": "minecraft:enchant_with_levels", "options": "#minecraft:on_random_loot",
                                                       "levels": {"type": "minecraft:uniform", "min": 20, "max": 39}}),
        ]}]})


# =========================================================================== data: receitas

def shaped(name, pattern, key, result, count=1, category="equipment"):
    write(DATA / NS / "recipe" / f"{name}.json", {"type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
                                                  "key": {k: ns(v) if not v.startswith("#") else "#" + ns(v[1:]) for k, v in key.items()},
                                                  "result": {"id": ns(result), "count": count} if count > 1 else {"id": ns(result)}})


def shapeless(name, ingredients, result, count=1, category="misc"):
    ing = [("#" + ns(i[1:])) if i.startswith("#") else ns(i) for i in ingredients]
    write(DATA / NS / "recipe" / f"{name}.json", {"type": "minecraft:crafting_shapeless", "category": category, "ingredients": ing,
                                                  "result": {"id": ns(result), "count": count} if count > 1 else {"id": ns(result)}})


def recipes():
    shapeless("pale_silk_planks", [f"#{NS}:pale_silk_logs"], "pale_silk_planks", 4, "building")
    shaped("fen_bricks", ["MM", "MM"], {"M": "void_mud"}, "fen_bricks", 4, "building")
    shapeless("echo_string", ["minecraft:echo_shard", "pale_silk_thread", "pale_silk_thread"], "echo_string")
    shaped("hollow_core", ["VRV", "SES", "VRV"], {"V": "vitreous_shard", "R": "resonant_shard", "S": "minecraft:shulker_shell",
                                                   "E": "minecraft:ender_eye"}, "hollow_core")
    # Exploração e mobilidade
    shaped("void_anchor", [" P ", "CRC", "OCO"], {"P": "minecraft:ender_pearl", "C": "minecraft:iron_chain", "R": "resonant_shard",
                                                  "O": "minecraft:crying_obsidian"}, "void_anchor")
    shaped("silk_glider", ["TCT", "T T", "T T"], {"T": "pale_silk_thread", "C": "minecraft:popped_chorus_fruit"}, "silk_glider")
    shaped("null_step_boots", ["S S", "R R"], {"S": "minecraft:shulker_shell", "R": "resonant_shard"}, "null_step_boots")
    shaped("resonance_compass", [" R ", "RCR", " R "], {"R": "resonant_shard", "C": "minecraft:compass"}, "resonance_compass")
    # Combate e ferramentas
    shaped("vitreous_cleaver", [" VV", " VV", "E  "], {"V": "vitreous_obsidian", "E": "minecraft:end_rod"}, "vitreous_cleaver")
    shaped("echo_string_bow", [" RX", "E X", " RX"], {"R": "minecraft:end_rod", "E": "resonant_shard", "X": "echo_string"}, "echo_string_bow")
    shaped("hollow_core_pickaxe", ["VHV", " E ", " E "], {"V": "vitreous_shard", "H": "hollow_core", "E": "minecraft:end_rod"}, "hollow_core_pickaxe")


# =========================================================================== data: tags

def tag(kind, namespace, name, values, replace=False):
    path = DATA / namespace / "tags" / kind / f"{name}.json"
    obj = {"values": values}
    if replace:
        obj["replace"] = True
    write(path, obj)


def tags():
    b = lambda *ids: [ns(i) for i in ids]
    tag("block", "minecraft", "mineable/pickaxe", b("vitreous_obsidian", "vitreous_shard_cluster", "updraft_vent", "resonant_spire",
                                                    "resonant_cluster", "fen_bricks", "cracked_fen_bricks"))
    tag("block", "minecraft", "mineable/axe", b("pale_silk_log", "pale_silk_planks"))
    tag("block", "minecraft", "mineable/shovel", b("void_mud", "pale_silk_moss"))
    tag("block", "minecraft", "mineable/hoe", b("pale_silk_leaves", "pale_silk_moss"))
    tag("block", "minecraft", "sword_efficient", b("luminous_silk"))
    tag("block", "minecraft", "needs_iron_tool", b("vitreous_obsidian", "updraft_vent"))
    tag("block", "minecraft", "logs_that_burn", b("pale_silk_log"))
    tag("block", "minecraft", "planks", b("pale_silk_planks"))
    tag("block", "minecraft", "leaves", b("pale_silk_leaves"))
    tag("block", "minecraft", "saplings", b("pale_silk_sapling"))
    tag("block", "minecraft", "replaceable_by_trees", b("voidreed"))
    tag("block", NS, "pale_silk_logs", b("pale_silk_log"))
    tag("block", NS, "supports_end_flora", ["minecraft:end_stone", ns("pale_silk_moss"), ns("void_mud"), "#minecraft:dirt"])
    tag("block", NS, "end_ground", ["minecraft:end_stone", ns("pale_silk_moss"), ns("void_mud"), ns("vitreous_obsidian")])
    tag("block", NS, "hollow_core_instant", ["minecraft:end_stone", "minecraft:end_stone_bricks", "minecraft:end_stone_brick_slab",
                                             "minecraft:end_stone_brick_stairs", "minecraft:end_stone_brick_wall", "minecraft:purpur_block",
                                             "minecraft:purpur_pillar", "minecraft:purpur_slab", "minecraft:purpur_stairs",
                                             "minecraft:chorus_plant", "minecraft:chorus_flower", "minecraft:end_rod",
                                             *b("pale_silk_moss", "vitreous_obsidian", "vitreous_shard_cluster", "resonant_spire",
                                                "resonant_cluster", "void_mud", "fen_bricks", "cracked_fen_bricks", "updraft_vent")])

    tag("item", "minecraft", "logs_that_burn", b("pale_silk_log"))
    tag("item", "minecraft", "planks", b("pale_silk_planks"))
    tag("item", "minecraft", "leaves", b("pale_silk_leaves"))
    tag("item", "minecraft", "saplings", b("pale_silk_sapling"))
    tag("item", "minecraft", "swords", b("vitreous_cleaver"))
    tag("item", "minecraft", "pickaxes", b("hollow_core_pickaxe"))
    tag("item", "minecraft", "cluster_max_harvestables", b("hollow_core_pickaxe"))
    tag("item", "minecraft", "foot_armor", b("null_step_boots"))
    tag("item", "minecraft", "enchantable/bow", b("echo_string_bow"))
    tag("item", "minecraft", "enchantable/durability", b("echo_string_bow", "silk_glider"))
    tag("item", "minecraft", "enchantable/equippable", b("silk_glider"))
    tag("item", NS, "pale_silk_logs", b("pale_silk_log"))
    tag("item", NS, "repairs_vitreous", b("vitreous_shard"))
    tag("item", NS, "repairs_hollow_core", b("hollow_core"))
    tag("item", NS, "repairs_null_step", ["minecraft:shulker_shell", ns("resonant_shard")])

    tag("entity_type", NS, "end_creatures", ["minecraft:enderman", "minecraft:endermite", "minecraft:shulker", "minecraft:ender_dragon"])

    biomes = b("pale_silk_woods", "vitreous_chasms", "resonant_spirefields", "void_fen")
    tag("worldgen/biome", NS, "hollow_horizons", biomes)
    tag("worldgen/biome", "minecraft", "is_end", biomes)
    tag("worldgen/biome", "minecraft", "has_structure/end_city", biomes)
    tag("worldgen/biome", NS, "has_structure/void_ruins", b("void_fen", "pale_silk_woods"))

    tag("worldgen/structure", NS, "resonance/end_city", ["minecraft:end_city"])
    tag("worldgen/structure", NS, "resonance/void_ruins", [ns("void_ruins")])


# =========================================================================== data: worldgen

def surface_placement(count=None, rarity=None, extra=None, offset_y=None, heightmap="WORLD_SURFACE_WG"):
    p = []
    if count is not None:
        p.append({"type": "minecraft:count", "count": count})
    if rarity is not None:
        p.append({"type": "minecraft:rarity_filter", "chance": rarity})
    p += [{"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": heightmap}]
    if offset_y is not None:
        p.append({"type": "minecraft:offset", "x": 0, "y": offset_y, "z": 0})
    p.append({"type": "minecraft:biome"})
    if extra:
        p += extra
    return p


def patch(count_inner, spread=6, vertical=2):
    return [{"type": "minecraft:count", "count": count_inner},
            {"type": "minecraft:offset",
             "x": {"type": "minecraft:trapezoid", "min": -spread, "max": spread, "plateau": 0},
             "y": {"type": "minecraft:trapezoid", "min": -vertical, "max": vertical, "plateau": 0},
             "z": {"type": "minecraft:trapezoid", "min": -spread, "max": spread, "plateau": 0}}]


def would_survive(block, properties=None):
    state = {"id": ns(block)}
    if properties:
        state["properties"] = properties
    return {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive", "state": state}}


def is_air():
    return {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_block_tag", "tag": "minecraft:air"}}


def worldgen():
    feat = DATA / NS / "worldgen/feature"
    placed = DATA / NS / "worldgen/placed_feature"

    def F(name, obj):
        write(feat / f"{name}.json", obj)

    def P(name, placement, feature=None):
        write(placed / f"{name}.json", {"feature": ns(feature or name), "placement": placement})

    silk_leaves = {"id": ns("pale_silk_leaves"), "properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}

    # ---------------------------------------------------------------- Florestas de Seda Pálida
    F("pale_silk_tree", {
        "type": "minecraft:tree",
        "below_trunk_provider": {"type": "minecraft:rule_based", "rules": [{
            "if_true": {"type": "minecraft:not", "predicate": {"type": "minecraft:matching_block_tag", "tag": "minecraft:cannot_replace_below_tree_trunk"}},
            "then": {"id": ns("pale_silk_moss")}}]},
        "trunk_provider": {"id": ns("pale_silk_log"), "properties": {"axis": "y"}},
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 9, "height_rand_a": 4, "height_rand_b": 3},
        "foliage_provider": silk_leaves,
        "foliage_placer": {"type": "minecraft:cherry_foliage_placer", "radius": 4, "offset": 0, "height": 5,
                           "wide_bottom_layer_hole_chance": 0.3, "corner_hole_chance": 0.3,
                           "hanging_leaves_chance": 0.3, "hanging_leaves_extension_chance": 0.5},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "upper_size": 2},
        "decorators": [{"type": "minecraft:attached_to_leaves", "probability": 0.12, "exclusion_radius_xz": 1, "exclusion_radius_y": 0,
                        "block_provider": {"id": ns("luminous_silk")}, "required_empty_blocks": 2, "directions": ["down"]}],
        "ignore_vines": True,
    })
    P("pale_silk_tree_checked", [would_survive("pale_silk_sapling", {"stage": "0"})], "pale_silk_tree")
    P("pale_silk_trees", surface_placement(count={"type": "minecraft:weighted_list", "distribution": [
        {"data": 6, "weight": 5}, {"data": 8, "weight": 3}, {"data": 10, "weight": 2}]}, extra=[would_survive("pale_silk_sapling", {"stage": "0"})]),
      "pale_silk_tree")
    F("luminous_silk_patch", {"type": "minecraft:simple_block", "to_place": {"id": ns("luminous_silk")}})
    P("luminous_silk_patch", surface_placement(rarity=3, extra=patch(6, 5, 1) + [is_air(), {
        "type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": ns("pale_silk_moss")}}]))

    # ---------------------------------------------------------------- Desfiladeiros de Obsidiana Vítrea
    F("chasm_rift", {"type": f"{NS}:chasm_rift", "wall": {"id": ns("vitreous_obsidian")}})
    P("chasm_rift", surface_placement(rarity=3))
    F("plasma_pool", {"type": f"{NS}:plasma_pool", "fluid": {"id": ns("void_plasma")}, "rim": {"id": ns("vitreous_obsidian")},
                      "radius": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4}})
    P("plasma_pool", surface_placement(rarity=2))
    F("vitreous_spire", {"type": f"{NS}:vitreous_spire", "body": {"id": ns("vitreous_obsidian")},
                         "tip": {"id": ns("vitreous_shard_cluster"), "properties": {"facing": "up", "waterlogged": "false"}},
                         "height": {"type": "minecraft:uniform", "min_inclusive": 7, "max_inclusive": 20}})
    P("vitreous_spire", surface_placement(count={"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 2}))
    F("updraft_vent", {"type": "minecraft:simple_block", "to_place": {"id": ns("updraft_vent")}})
    P("updraft_vent", surface_placement(rarity=2, offset_y=-1, extra=[{
        "type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
            {"type": "minecraft:matching_block_tag", "tag": f"{NS}:end_ground"},
            {"type": "minecraft:matching_block_tag", "offset": [0, 1, 0], "tag": "minecraft:air"}]}}]))
    F("vitreous_shard_cluster", {"type": "minecraft:simple_block",
                                 "to_place": {"id": ns("vitreous_shard_cluster"), "properties": {"facing": "up", "waterlogged": "false"}}})
    P("vitreous_shard_cluster", surface_placement(count=2, extra=patch(4, 5, 1) + [is_air(), {
        "type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_block_tag", "offset": [0, -1, 0], "tag": f"{NS}:end_ground"}}]))

    # ---------------------------------------------------------------- Campos de Cristal Ressonante
    F("resonant_spire", {"type": f"{NS}:resonant_spire", "spire": {"id": ns("resonant_spire"), "properties": {"axis": "y"}},
                         "cluster": {"id": ns("resonant_cluster"), "properties": {"facing": "up", "waterlogged": "false"}},
                         "height": {"type": "minecraft:uniform", "min_inclusive": 6, "max_inclusive": 15}})
    P("resonant_spires", surface_placement(count={"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4}), "resonant_spire")
    F("resonant_cluster", {"type": "minecraft:simple_block",
                           "to_place": {"id": ns("resonant_cluster"), "properties": {"facing": "up", "waterlogged": "false"}}})
    P("resonant_cluster_patch", surface_placement(count=3, extra=patch(5, 6, 1) + [is_air(), {
        "type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_block_tag", "offset": [0, -1, 0], "tag": f"{NS}:end_ground"}}]), "resonant_cluster")

    # ---------------------------------------------------------------- Pântanos do Vazio
    F("fen_pool", {"type": "minecraft:waterlogged_vegetation_patch", "surface": "floor", "replaceable": f"#{NS}:end_ground",
                   "ground_state": {"id": ns("void_mud")}, "depth": 2, "extra_bottom_block_chance": 0.6, "extra_edge_column_chance": 0.5,
                   "vertical_range": 4, "vegetation_chance": 0.0, "xz_radius": {"type": "minecraft:uniform", "min_inclusive": 3, "max_inclusive": 6},
                   "vegetation_feature": {"feature": ns("voidreed"), "placement": []}})
    P("fen_pools", surface_placement(count={"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4}, offset_y=-1), "fen_pool")
    F("glowfen_lily", {"type": "minecraft:simple_block", "to_place": {"id": ns("glowfen_lily")}})
    P("glowfen_lily_patch", surface_placement(count=3, extra=patch(8, 7, 2) + [is_air(), would_survive("glowfen_lily")]), "glowfen_lily")
    F("voidreed", {"type": "minecraft:simple_block", "to_place": {"id": ns("voidreed")}})
    P("voidreed_patch", surface_placement(count=4, extra=patch(10, 6, 2) + [is_air(), would_survive("voidreed")]), "voidreed")
    F("fen_ruin", {"type": f"{NS}:fen_ruin", "bricks": {"id": ns("fen_bricks")}, "cracked": {"id": ns("cracked_fen_bricks")}})
    P("fen_ruin", surface_placement(rarity=6))

    # ---------------------------------------------------------------- biomas
    enderman = {"minecraft:gameplay/natural_mob_spawns": {"modifier": "overlay", "argument": {"spawn_costs": {}, "spawns_by_category": {
        "monster": [{"type": "minecraft:enderman", "weight": 10, "count": 4}]}}}}

    def biome(name, attributes, features, water="#3f76e4"):
        steps = [[] for _ in range(11)]
        for step, ids in features.items():
            steps[step] = ids
        while steps and not steps[-1]:
            steps.pop()
        write(DATA / NS / "worldgen/biome" / f"{name}.json", {
            "attributes": {**enderman, **attributes},
            "carvers": [], "downfall": 0.5, "effects": {"water_color": water}, "features": steps,
            "has_precipitation": False, "temperature": 0.5})

    def particles(particle, probability):
        return {"minecraft:visual/ambient_particles": {"modifier": "append", "argument": [{"particle": {"type": particle}, "probability": probability}]}}

    def additions(sound, chance):
        return {"minecraft:audio/ambient_sounds": {"additions": {"sound": sound, "tick_chance": chance},
                                                   "mood": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}}}

    gateway = "minecraft:end_gateway_return"
    biome("pale_silk_woods", {**particles(f"{NS}:silk_spore", 0.02), **additions("minecraft:block.amethyst_block.resonate", 0.004),
                              "minecraft:visual/fog_color": "#3b3450"},
          {4: [gateway], 9: [ns("pale_silk_trees"), ns("luminous_silk_patch")]}, water="#d9d4ff")
    biome("vitreous_chasms", {**particles("minecraft:reverse_portal", 0.006), **additions("minecraft:ambient.basalt_deltas.additions", 0.0111),
                              "minecraft:visual/fog_color": "#1a0f2b"},
          {2: [ns("chasm_rift"), ns("plasma_pool")], 4: [gateway, ns("vitreous_spire")], 9: [ns("updraft_vent"), ns("vitreous_shard_cluster")]})
    biome("resonant_spirefields", {**particles("minecraft:end_rod", 0.003), **additions("minecraft:block.amethyst_block.chime", 0.02),
                                   "minecraft:visual/fog_color": "#2c2840"},
          {4: [gateway, ns("resonant_spires")], 9: [ns("resonant_cluster_patch"), "minecraft:chorus_plant"]})
    biome("void_fen", {**particles("minecraft:portal", 0.01), **additions("minecraft:ambient.soul_sand_valley.mood", 0.003),
                       "minecraft:visual/fog_color": "#16222a",
                       "minecraft:visual/fog_end_distance": {"modifier": "multiply", "argument": 0.35},
                       "minecraft:visual/water_fog_color": "#120c1f",
                       "minecraft:visual/water_fog_end_distance": {"modifier": "multiply", "argument": 0.5}},
          {1: [ns("fen_pools")], 4: [gateway, ns("fen_ruin")], 9: [ns("glowfen_lily_patch"), ns("voidreed_patch")]}, water="#1b1430")

    # ---------------------------------------------------------------- superfície (regra de material do End)
    def floor_rule(biome_id, block, under=False):
        cond = "minecraft:under_floor" if under else "minecraft:on_floor"
        return {"type": "minecraft:condition", "if_true": {"type": "minecraft:biome", "biome_is": ns(biome_id)},
                "then_run": {"type": "minecraft:condition", "if_true": cond, "then_run": {"type": "minecraft:block", "result_state": ns(block)}}}

    vitreous_patches = {"type": "minecraft:condition", "if_true": {"type": "minecraft:biome", "biome_is": ns("vitreous_chasms")},
                        "then_run": {"type": "minecraft:condition", "if_true": "minecraft:on_floor",
                                     "then_run": {"type": "minecraft:condition",
                                                  "if_true": {"type": "minecraft:noise_threshold", "noise": "minecraft:patch",
                                                              "min_threshold": 0.05, "max_threshold": 1.7976931348623157e308},
                                                  "then_run": {"type": "minecraft:block", "result_state": ns("vitreous_obsidian")}}}}
    write(DATA / "minecraft/worldgen/material_rule/end.json", {"type": "minecraft:sequence", "sequence": [
        floor_rule("pale_silk_woods", "pale_silk_moss"),
        floor_rule("void_fen", "void_mud", under=True),
        vitreous_patches,
        {"type": "minecraft:block", "result_state": "minecraft:end_stone"}]})

    # ---------------------------------------------------------------- estrutura: Ruínas do Vazio
    write(DATA / NS / "worldgen/structure/void_ruins.json", {"type": f"{NS}:void_ruins", "biomes": f"#{NS}:has_structure/void_ruins",
                                                            "spawn_overrides": {}, "step": "surface_structures"})
    write(DATA / NS / "worldgen/structure_set/void_ruins.json", {"structures": [{"structure": ns("void_ruins"), "weight": 1}],
                                                                "placement": {"type": "minecraft:random_spread", "salt": 26042026,
                                                                              "spacing": 18, "separation": 6}})


def main():
    for d in (ASSETS / "blockstates", ASSETS / "models", ASSETS / "items", ASSETS / "equipment", ASSETS / "particles", ASSETS / "lang",
              DATA / NS, DATA / "minecraft"):
        if d.exists():
            shutil.rmtree(d)
    blockstate_and_models()
    item_models()
    equipment_particles_lang()
    loot_tables()
    recipes()
    tags()
    worldgen()
    files = [p for p in RES.rglob("*.json") if "assets/hollowhorizons/textures" not in str(p)]
    print(f"{len(files)} arquivos JSON gerados em {RES.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
