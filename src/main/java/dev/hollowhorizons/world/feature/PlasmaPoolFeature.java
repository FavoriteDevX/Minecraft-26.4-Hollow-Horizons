package dev.hollowhorizons.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Lago de plasma roxo com borda e fundo de Obsidiana Vítrea. Só nasce em chão firme: se
 * alguma parte do lago ficaria sobre o vazio (beira da ilha), a feature desiste.
 */
public record PlasmaPoolFeature(BlockState fluid, BlockState rim, IntProvider radius) implements Feature {
	public static final MapCodec<PlasmaPoolFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BlockState.CODEC.fieldOf("fluid").forGetter(PlasmaPoolFeature::fluid),
		BlockState.CODEC.fieldOf("rim").forGetter(PlasmaPoolFeature::rim),
		IntProviders.codec(1, 8).fieldOf("radius").forGetter(PlasmaPoolFeature::radius)
	).apply(i, PlasmaPoolFeature::new));

	@Override
	public MapCodec<PlasmaPoolFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int rx = this.radius.sample(random);
		int rz = Math.max(1, rx + random.nextInt(3) - 1);
		int depth = 2 + random.nextInt(2);
		BlockPos floor = origin.below();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		// 1) Checagem: chão sólido sob todo o lago e a borda.
		for (int dx = -rx - 1; dx <= rx + 1; dx++) {
			for (int dz = -rz - 1; dz <= rz + 1; dz++) {
				if (!inside(dx, dz, rx + 1, rz + 1)) {
					continue;
				}
				for (int dy = 0; dy <= depth; dy++) {
					pos.setWithOffset(floor, dx, -dy, dz);
					if (!FeatureUtil.isGround(level.getBlockState(pos))) {
						return false;
					}
				}
			}
		}

		// 2) Escava, enche e reveste.
		for (int dx = -rx - 1; dx <= rx + 1; dx++) {
			for (int dz = -rz - 1; dz <= rz + 1; dz++) {
				boolean pool = inside(dx, dz, rx, rz);
				boolean rimCell = !pool && inside(dx, dz, rx + 1, rz + 1);
				if (pool) {
					for (int dy = 0; dy < depth; dy++) {
						FeatureUtil.set(level, pos.setWithOffset(floor, dx, -dy, dz), this.fluid);
					}
					FeatureUtil.set(level, pos.setWithOffset(floor, dx, -depth, dz), this.rim);
					BlockPos above = floor.offset(dx, 1, dz);
					if (!level.getBlockState(above).isAir()) {
						FeatureUtil.set(level, above, Blocks.AIR.defaultBlockState());
					}
				} else if (rimCell) {
					for (int dy = 0; dy <= depth; dy++) {
						if (random.nextFloat() < 0.9F) {
							FeatureUtil.set(level, pos.setWithOffset(floor, dx, -dy, dz), this.rim);
						}
					}
				}
			}
		}
		return true;
	}

	private static boolean inside(int dx, int dz, int rx, int rz) {
		double nx = dx / (rx + 0.5);
		double nz = dz / (rz + 0.5);
		return nx * nx + nz * nz <= 1.0;
	}
}
