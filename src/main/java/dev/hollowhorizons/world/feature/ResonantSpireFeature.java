package dev.hollowhorizons.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Formação de quartzo ressonante: uma torre central grossa cercada de agulhas menores,
 * cada uma coroada por um aglomerado que canta com o vento.
 */
public record ResonantSpireFeature(BlockState spire, BlockState cluster, IntProvider height) implements Feature {
	public static final MapCodec<ResonantSpireFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BlockState.CODEC.fieldOf("spire").forGetter(ResonantSpireFeature::spire),
		BlockState.CODEC.fieldOf("cluster").forGetter(ResonantSpireFeature::cluster),
		IntProviders.codec(3, 24).fieldOf("height").forGetter(ResonantSpireFeature::height)
	).apply(i, ResonantSpireFeature::new));

	@Override
	public MapCodec<ResonantSpireFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		if (!FeatureUtil.isGround(level.getBlockState(origin.below()))) {
			return false;
		}
		int mainHeight = this.height.sample(random);
		column(level, random, origin, mainHeight, true);

		int satellites = 3 + random.nextInt(4);
		for (int i = 0; i < satellites; i++) {
			int dx = random.nextInt(9) - 4;
			int dz = random.nextInt(9) - 4;
			if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
				continue;
			}
			int top = FeatureUtil.surface(level, origin.getX() + dx, origin.getZ() + dz);
			if (top == Integer.MIN_VALUE || Math.abs(top - origin.getY()) > 4) {
				continue;
			}
			BlockPos base = new BlockPos(origin.getX() + dx, top, origin.getZ() + dz);
			if (FeatureUtil.isGround(level.getBlockState(base.below()))) {
				column(level, random, base, Math.max(2, mainHeight / 2 - random.nextInt(3)), false);
			}
		}
		return true;
	}

	private void column(WorldGenLevel level, RandomSource random, BlockPos base, int height, boolean thick) {
		BlockState body = this.spire.hasProperty(RotatedPillarBlock.AXIS) ? this.spire.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : this.spire;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dy = -1; dy < height; dy++) {
			pos.setWithOffset(base, 0, dy, 0);
			if (FeatureUtil.replaceable(level.getBlockState(pos))) {
				FeatureUtil.set(level, pos, body);
			}
			// Torre central: cruz de 5 blocos até 2/3 da altura.
			if (thick && dy < height * 2 / 3) {
				for (Direction side : Direction.Plane.HORIZONTAL) {
					pos.setWithOffset(base, side.getStepX(), dy, side.getStepZ());
					if (FeatureUtil.replaceable(level.getBlockState(pos))) {
						FeatureUtil.set(level, pos, body);
					}
				}
			}
		}
		placeCluster(level, base.above(height), Direction.UP);
		// Cristais laterais brotando das faces.
		for (int i = 0; i < (thick ? 4 : 1); i++) {
			Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			int y = 1 + random.nextInt(Math.max(1, height - 1));
			BlockPos sidePos = base.above(y).relative(side, thick && y < height * 2 / 3 ? 2 : 1);
			placeCluster(level, sidePos, side);
		}
	}

	private void placeCluster(WorldGenLevel level, BlockPos pos, Direction facing) {
		if (level.getBlockState(pos).isAir()) {
			BlockState state = this.cluster.hasProperty(AmethystClusterBlock.FACING) ? this.cluster.setValue(AmethystClusterBlock.FACING, facing) : this.cluster;
			FeatureUtil.set(level, pos, state);
		}
	}
}
