package dev.hollowhorizons.world.structure;

import dev.hollowhorizons.registry.HHBlocks;
import dev.hollowhorizons.registry.HHWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import dev.hollowhorizons.HollowHorizons;

/**
 * Peça única das Ruínas do Vazio (13×9×13): piso de tijolos do pântano afundado na lama,
 * um anel de pilares quebrados, poças de água escura, lírios brilhantes e um altar com baú.
 */
public class VoidRuinsPiece extends ScatteredFeaturePiece {
	public static final int WIDTH = 13;
	public static final int HEIGHT = 9;
	public static final int DEPTH = 13;
	public static final ResourceKey<LootTable> CHEST_LOOT = ResourceKey.create(Registries.LOOT_TABLE, HollowHorizons.id("chests/void_ruins"));

	private final long seed;

	public VoidRuinsPiece(RandomSource random, BlockPos start) {
		super(HHWorldgen.VOID_RUINS_PIECE, start.getX(), start.getY(), start.getZ(), WIDTH, HEIGHT, DEPTH, getRandomHorizontalDirection(random));
		this.seed = random.nextLong();
	}

	public VoidRuinsPiece(CompoundTag tag) {
		super(HHWorldgen.VOID_RUINS_PIECE, tag);
		this.seed = tag.getLongOr("Seed", 0L);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putLong("Seed", this.seed);
	}

	private BlockState brick(RandomSource random) {
		return (random.nextFloat() < 0.3F ? HHBlocks.CRACKED_FEN_BRICKS : HHBlocks.FEN_BRICKS).defaultBlockState();
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource ignored,
		BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
		// Aleatoriedade estável por peça: o mesmo desenho em todos os chunks que ela toca.
		RandomSource random = RandomSource.create(this.seed);
		BlockState mud = HHBlocks.VOID_MUD.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState water = Blocks.WATER.defaultBlockState();

		// Fundação: lama sob todo o templo, preenchendo até o chão (no máximo 12 blocos:
		// nunca cria um pilar até o fundo do mundo se houver um buraco sob a ruína).
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				for (int y = -1; y >= -12; y--) {
					BlockState below = this.getBlock(level, x, y, z, chunkBB);
					if (!below.isAir() && !below.liquid()) {
						break;
					}
					this.placeBlock(level, mud, x, y, z, chunkBB);
				}
			}
		}

		// Piso de tijolos com falhas de lama e poças.
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				float roll = random.nextFloat();
				BlockState floor = roll < 0.12F ? mud : brick(random);
				this.placeBlock(level, floor, x, 0, z, chunkBB);
				for (int y = 1; y < HEIGHT; y++) {
					this.placeBlock(level, air, x, y, z, chunkBB);
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			int px = 1 + random.nextInt(WIDTH - 2);
			int pz = 1 + random.nextInt(DEPTH - 2);
			if (Math.abs(px - WIDTH / 2) > 2 || Math.abs(pz - DEPTH / 2) > 2) {
				this.placeBlock(level, water, px, 0, pz, chunkBB);
				this.placeBlock(level, HHBlocks.GLOWFEN_LILY.defaultBlockState(), px, 1, pz, chunkBB);
			}
		}

		// Anel de oito pilares quebrados.
		int[][] pillars = {{1, 1}, {6, 0}, {11, 1}, {12, 6}, {11, 11}, {6, 12}, {1, 11}, {0, 6}};
		for (int[] p : pillars) {
			int height = 2 + random.nextInt(HEIGHT - 3);
			for (int y = 1; y <= height; y++) {
				this.placeBlock(level, brick(random), p[0], y, p[1], chunkBB);
			}
		}

		// Arcos parciais ligando alguns pilares.
		for (int x = 2; x <= 10; x++) {
			if (random.nextFloat() < 0.55F) {
				this.placeBlock(level, brick(random), x, 5, 1, chunkBB);
			}
			if (random.nextFloat() < 0.55F) {
				this.placeBlock(level, brick(random), x, 5, 11, chunkBB);
			}
		}

		// Altar central: degraus, pedestal de obsidiana vítrea e o baú.
		int cx = WIDTH / 2;
		int cz = DEPTH / 2;
		for (int x = cx - 2; x <= cx + 2; x++) {
			for (int z = cz - 2; z <= cz + 2; z++) {
				this.placeBlock(level, HHBlocks.FEN_BRICKS.defaultBlockState(), x, 1, z, chunkBB);
			}
		}
		for (int x = cx - 1; x <= cx + 1; x++) {
			for (int z = cz - 1; z <= cz + 1; z++) {
				this.placeBlock(level, HHBlocks.VITREOUS_OBSIDIAN.defaultBlockState(), x, 2, z, chunkBB);
			}
		}
		this.createChest(level, chunkBB, random, cx, 3, cz, CHEST_LOOT);

		// Cristais ressonantes crescendo nos cantos do altar: a ruína "canta" para a bússola.
		BlockState cluster = HHBlocks.RESONANT_CLUSTER.defaultBlockState();
		this.placeBlock(level, cluster, cx - 2, 2, cz - 2, chunkBB);
		this.placeBlock(level, cluster, cx + 2, 2, cz + 2, chunkBB);
		this.placeBlock(level, HHBlocks.LUMINOUS_SILK.defaultBlockState(), cx + 2, 2, cz - 2, chunkBB);
		this.placeBlock(level, HHBlocks.VOIDREED.defaultBlockState(), cx - 2, 2, cz + 2, chunkBB);

		// Juncos crescendo na lama dentro do templo.
		for (int i = 0; i < 6; i++) {
			int rx = random.nextInt(WIDTH);
			int rz = random.nextInt(DEPTH);
			if (this.getBlock(level, rx, 0, rz, chunkBB).is(HHBlocks.VOID_MUD) && this.getBlock(level, rx, 1, rz, chunkBB).isAir()) {
				this.placeBlock(level, HHBlocks.VOIDREED.defaultBlockState(), rx, 1, rz, chunkBB);
			}
		}
	}

	@Override
	protected boolean canBeReplaced(net.minecraft.world.level.LevelReader level, int x, int y, int z, BoundingBox chunkBB) {
		return true;
	}

}
