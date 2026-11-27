package dev.hollowhorizons.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Penhasco afiado de vidro negro: um cone torto e serrilhado de Obsidiana Vítrea, com um
 * aglomerado de fragmentos na ponta. Dá aos Desfiladeiros o perfil de "ilhas partidas".
 */
public record VitreousSpireFeature(BlockState body, BlockState tip, IntProvider height) implements Feature {
	public static final MapCodec<VitreousSpireFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BlockState.CODEC.fieldOf("body").forGetter(VitreousSpireFeature::body),
		BlockState.CODEC.fieldOf("tip").forGetter(VitreousSpireFeature::tip),
		IntProviders.codec(4, 32).fieldOf("height").forGetter(VitreousSpireFeature::height)
	).apply(i, VitreousSpireFeature::new));

	@Override
	public MapCodec<VitreousSpireFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		if (!FeatureUtil.isGround(level.getBlockState(origin.below()))) {
			return false;
		}
		int height = this.height.sample(random);
		float baseRadius = 1.4F + random.nextFloat() * 1.6F;
		float leanX = (random.nextFloat() - 0.5F) * 0.45F;
		float leanZ = (random.nextFloat() - 0.5F) * 0.45F;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos top = origin;

		for (int dy = -3; dy <= height; dy++) {
			float t = Math.max(0.0F, (float) dy / height);
			float radius = baseRadius * (float) Math.pow(1.0F - t, 1.25F);
			double cx = origin.getX() + 0.5 + leanX * Math.max(0, dy);
			double cz = origin.getZ() + 0.5 + leanZ * Math.max(0, dy);
			int r = Mth.ceil(radius) + 1;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					int x = Mth.floor(cx) + dx;
					int z = Mth.floor(cz) + dz;
					double dist = Math.sqrt((x + 0.5 - cx) * (x + 0.5 - cx) + (z + 0.5 - cz) * (z + 0.5 - cz));
					// Serrilhado: cada camada "morde" o raio de forma diferente.
					if (dist <= radius + random.nextFloat() * 0.45F - 0.15F || (dx == 0 && dz == 0 && dy < height)) {
						pos.set(x, origin.getY() + dy, z);
						if (FeatureUtil.replaceable(level.getBlockState(pos))) {
							FeatureUtil.set(level, pos, this.body);
							if (pos.getY() > top.getY()) {
								top = pos.immutable();
							}
						}
					}
				}
			}
		}

		BlockPos tipPos = top.above();
		if (level.getBlockState(tipPos).isAir()) {
			BlockState tipState = this.tip.hasProperty(AmethystClusterBlock.FACING) ? this.tip.setValue(AmethystClusterBlock.FACING, Direction.UP) : this.tip;
			FeatureUtil.set(level, tipPos, tipState);
		}
		return true;
	}
}
