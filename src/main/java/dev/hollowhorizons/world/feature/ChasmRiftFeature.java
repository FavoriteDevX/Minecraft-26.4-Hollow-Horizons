package dev.hollowhorizons.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Fenda: um racho serrilhado que atravessa a ilha de cima a baixo — o "desfiladeiro".
 * As paredes expostas viram Obsidiana Vítrea, como se o vidro tivesse escorrido ao rachar.
 */
public record ChasmRiftFeature(BlockState wall) implements Feature {
	public static final MapCodec<ChasmRiftFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BlockState.CODEC.fieldOf("wall").forGetter(ChasmRiftFeature::wall)
	).apply(i, ChasmRiftFeature::new));

	private static final int MAX_DEPTH = 56;

	@Override
	public MapCodec<ChasmRiftFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		double angle = random.nextDouble() * Math.PI * 2.0;
		int half = 5 + random.nextInt(7);
		double maxWidth = 1.3 + random.nextDouble() * 1.4;
		Set<Long> carved = new HashSet<>();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		double wobble = 0.0;

		for (double s = -half; s <= half; s += 0.5) {
			wobble = Mth.clamp(wobble + (random.nextDouble() - 0.5) * 0.6, -1.5, 1.5);
			double cx = origin.getX() + Math.cos(angle) * s - Math.sin(angle) * wobble;
			double cz = origin.getZ() + Math.sin(angle) * s + Math.cos(angle) * wobble;
			double width = maxWidth * (1.0 - Math.abs(s) / (half + 1.0)) + 0.4;
			int r = Mth.ceil(width);
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (dx * dx + dz * dz > width * width) {
						continue;
					}
					int x = Mth.floor(cx) + dx;
					int z = Mth.floor(cz) + dz;
					if (!carved.add(BlockPos.asLong(x, 0, z))) {
						continue;
					}
					int top = FeatureUtil.surface(level, x, z);
					if (top == Integer.MIN_VALUE) {
						continue;
					}
					for (int y = top; y > top - MAX_DEPTH && y > level.getMinY(); y--) {
						pos.set(x, y, z);
						BlockState state = level.getBlockState(pos);
						if (FeatureUtil.isGround(state) || state.canBeReplaced()) {
							FeatureUtil.set(level, pos, Blocks.AIR.defaultBlockState());
						} else if (!state.isAir()) {
							break;
						}
					}
				}
			}
		}

		// Paredes vítreas onde o racho expôs a pedra.
		for (long packed : carved) {
			int x = BlockPos.getX(packed);
			int z = BlockPos.getZ(packed);
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				int nx = x + direction.getStepX();
				int nz = z + direction.getStepZ();
				if (carved.contains(BlockPos.asLong(nx, 0, nz))) {
					continue;
				}
				int top = FeatureUtil.surface(level, nx, nz);
				if (top == Integer.MIN_VALUE) {
					continue;
				}
				for (int y = top - 1; y > top - MAX_DEPTH && y > level.getMinY(); y--) {
					pos.set(nx, y, nz);
					BlockState state = level.getBlockState(pos);
					if (!FeatureUtil.isGround(state)) {
						if (state.isAir()) {
							break;
						}
						continue;
					}
					if (random.nextFloat() < 0.85F) {
						FeatureUtil.set(level, pos, this.wall);
					}
				}
			}
		}
		return !carved.isEmpty();
	}
}
