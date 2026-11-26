package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class HHTags {
	private HHTags() {}

	public static final class Blocks {
		/** Blocos que a Picareta de Núcleo Oco quebra instantaneamente. */
		public static final TagKey<Block> HOLLOW_CORE_INSTANT = TagKey.create(Registries.BLOCK, HollowHorizons.id("hollow_core_instant"));
		/** Onde a flora do End (mudas, juncos) pode nascer. */
		public static final TagKey<Block> SUPPORTS_END_FLORA = TagKey.create(Registries.BLOCK, HollowHorizons.id("supports_end_flora"));
		/** Superfícies que as features dos biomas novos podem substituir. */
		public static final TagKey<Block> END_GROUND = TagKey.create(Registries.BLOCK, HollowHorizons.id("end_ground"));
		public static final TagKey<Block> PALE_SILK_LOGS = TagKey.create(Registries.BLOCK, HollowHorizons.id("pale_silk_logs"));

		private Blocks() {}
	}

	public static final class Items {
		public static final TagKey<Item> PALE_SILK_LOGS = TagKey.create(Registries.ITEM, HollowHorizons.id("pale_silk_logs"));
		public static final TagKey<Item> REPAIRS_VITREOUS = TagKey.create(Registries.ITEM, HollowHorizons.id("repairs_vitreous"));
		public static final TagKey<Item> REPAIRS_HOLLOW_CORE = TagKey.create(Registries.ITEM, HollowHorizons.id("repairs_hollow_core"));
		public static final TagKey<Item> REPAIRS_NULL_STEP = TagKey.create(Registries.ITEM, HollowHorizons.id("repairs_null_step"));

		private Items() {}
	}

	public static final class Entities {
		/** Criaturas do End: a Lâmina de Obsidiana Vítrea causa dano extra nelas. */
		public static final TagKey<EntityType<?>> END_CREATURES = TagKey.create(Registries.ENTITY_TYPE, HollowHorizons.id("end_creatures"));

		private Entities() {}
	}

	public static final class Structures {
		public static final TagKey<Structure> RESONANCE_END_CITY = TagKey.create(Registries.STRUCTURE, HollowHorizons.id("resonance/end_city"));
		public static final TagKey<Structure> RESONANCE_VOID_RUINS = TagKey.create(Registries.STRUCTURE, HollowHorizons.id("resonance/void_ruins"));

		private Structures() {}
	}

	public static final class Biomes {
		public static final TagKey<Biome> HOLLOW_HORIZONS = TagKey.create(Registries.BIOME, HollowHorizons.id("hollow_horizons"));

		private Biomes() {}
	}
}
