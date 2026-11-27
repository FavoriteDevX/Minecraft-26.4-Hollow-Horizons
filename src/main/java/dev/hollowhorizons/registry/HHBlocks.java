package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import dev.hollowhorizons.block.EndFloraBlock;
import dev.hollowhorizons.block.GlowfenLilyBlock;
import dev.hollowhorizons.block.LuminousSilkBlock;
import dev.hollowhorizons.block.PaleSilkSaplingBlock;
import dev.hollowhorizons.block.ResonantClusterBlock;
import dev.hollowhorizons.block.ResonantSpireBlock;
import dev.hollowhorizons.block.UpdraftVentBlock;
import dev.hollowhorizons.block.VoidPlasmaBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Todos os blocos novos do The End. */
public final class HHBlocks {
	public static final TreeGrower PALE_SILK_GROWER = new TreeGrower(
		"hollowhorizons_pale_silk",
		WeightedList.of(HHWorldgen.PALE_SILK_TREE),
		WeightedList.<ResourceKey<Feature>>of(), WeightedList.<ResourceKey<Feature>>of(), null);

	// ---------------------------------------------------------------- Florestas de Seda Pálida
	public static final Block PALE_SILK_LOG = register("pale_silk_log", RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASS)
			.strength(2.0F).sound(SoundType.CHERRY_WOOD).ignitedByLava());
	public static final Block PALE_SILK_PLANKS = register("pale_silk_planks", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F).sound(SoundType.CHERRY_WOOD).ignitedByLava());
	public static final Block PALE_SILK_LEAVES = register("pale_silk_leaves",
		p -> new UntintedParticleLeavesBlock(0.015F, HHParticles.SILK_SPORE, AmbientLeavesBlockSoundPlayer.noAmbientSound(), p),
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.2F).randomTicks().sound(SoundType.CHERRY_LEAVES)
			.noOcclusion().lightLevel(s -> 7).isValidSpawn(Blocks::never).isSuffocating(Blocks::never)
			.ignitedByLava().pushReaction(PushReaction.POPPED).isRedstoneConductor(Blocks::never));
	public static final Block PALE_SILK_SAPLING = register("pale_silk_sapling",
		p -> new PaleSilkSaplingBlock(PALE_SILK_GROWER, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).noCollision().randomTicks().instabreak()
			.sound(SoundType.CHERRY_SAPLING).lightLevel(s -> 4).pushReaction(PushReaction.POPPED));
	public static final Block LUMINOUS_SILK = register("luminous_silk", LuminousSilkBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).sound(SoundType.COBWEB).forceSolidOn().noCollision()
			.requiresCorrectToolForDrops().strength(2.0F).lightLevel(s -> 10).pushReaction(PushReaction.POPPED));
	public static final Block PALE_SILK_MOSS = register("pale_silk_moss", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASEDRUM)
			.strength(1.2F, 4.0F).sound(SoundType.MOSS));

	// ---------------------------------------------------------------- Desfiladeiros de Obsidiana Vítrea
	public static final Block VITREOUS_OBSIDIAN = register("vitreous_obsidian", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops().strength(6.0F, 600.0F).sound(SoundType.GLASS));
	public static final Block VITREOUS_SHARD_CLUSTER = register("vitreous_shard_cluster",
		p -> new ResonantClusterBlock(7.0F, 3.0F, false, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).forceSolidOn().noOcclusion()
			.sound(SoundType.AMETHYST_CLUSTER).strength(1.5F).lightLevel(s -> 3).pushReaction(PushReaction.POPPED));
	public static final Block VOID_PLASMA = register("void_plasma", VoidPlasmaBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).replaceable().noCollision().noOcclusion()
			.strength(100.0F).lightLevel(s -> 13).noLootTable().sound(SoundType.HONEY_BLOCK).pushReaction(PushReaction.POPPED)
			.isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never));
	public static final Block UPDRAFT_VENT = register("updraft_vent", UpdraftVentBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops().strength(5.0F, 600.0F).sound(SoundType.GLASS).lightLevel(s -> 6));

	// ---------------------------------------------------------------- Campos de Cristal Ressonante
	public static final Block RESONANT_SPIRE = register("resonant_spire", ResonantSpireBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.CHIME)
			.requiresCorrectToolForDrops().strength(1.5F).sound(SoundType.AMETHYST).lightLevel(s -> 2));
	public static final Block RESONANT_CLUSTER = register("resonant_cluster",
		p -> new ResonantClusterBlock(7.0F, 3.0F, true, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().noOcclusion()
			.sound(SoundType.AMETHYST_CLUSTER).strength(1.5F).lightLevel(s -> 6).pushReaction(PushReaction.POPPED));

	// ---------------------------------------------------------------- Pântanos do Vazio
	public static final Block VOID_MUD = register("void_mud", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN).strength(0.5F).sound(SoundType.MUD));
	public static final Block GLOWFEN_LILY = register("glowfen_lily", GlowfenLilyBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instabreak().sound(SoundType.LILY_PAD).noOcclusion()
			.lightLevel(s -> 10).pushReaction(PushReaction.POPPED));
	public static final Block VOIDREED = register("voidreed", EndFloraBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).replaceable().noCollision().instabreak()
			.sound(SoundType.GRASS).lightLevel(s -> 5).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED));
	public static final Block FEN_BRICKS = register("fen_bricks", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.MUD_BRICKS));
	public static final Block CRACKED_FEN_BRICKS = register("cracked_fen_bricks", Block::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.MUD_BRICKS));

	private HHBlocks() {}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, HollowHorizons.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	public static void init() {}
}
