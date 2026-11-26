package dev.hollowhorizons.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ruína meio submersa: pilares quebrados, um pedaço de muro e uma coluna tombada, afundados
 * na lama do Pântano do Vazio — restos de quem viveu no End antes dos endermen.
 */
public record FenRuinFeature(BlockState bricks, BlockState cracked) implements Feature {
	public static final MapCodec<FenRuinFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BlockState.CODEC.fieldOf("bricks").forGetter(FenRuinFeature::bricks),
		BlockState.CODEC.fieldOf("cracked").forGetter(FenRuinFeature::cracked)
	).apply(i, FenRuinFeature::new));

	@Override
	public MapCodec<FenRuinFeature> codec() {
		return CODEC;
	}

	private BlockState brick(RandomSource random) {
		return random.nextFloat() < 0.3F ? this.cracked : this.bricks;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		if (!FeatureUtil.isGround(level.getBlockState(origin.below()))) {
			return false;
		}
		int size = 2 + random.nextInt(2);
		BlockPos base = origin.below(2);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		// Piso afundado (só onde há chão embaixo).
		for (int dx = -size; dx <= size; dx++) {
			for (int dz = -size; dz <= size; dz++) {
				pos.setWithOffset(base, dx, 0, dz);
				if (FeatureUtil.isGround(level.getBlockState(pos)) && random.nextFloat() < 0.8F) {
					FeatureUtil.set(level, pos, brick(random));
				}
			}
		}

		// Pilares nos quatro cantos, cada um quebrado numa altura diferente.
		int[][] corners = {{-size, -size}, {size, -size}, {-size, size}, {size, size}};
		for (int[] corner : corners) {
			int height = random.nextInt(6);
			for (int dy = 1; dy <= height; dy++) {
				pos.setWithOffset(base, corner[0], dy, corner[1]);
				if (FeatureUtil.replaceable(level.getBlockState(pos))) {
					FeatureUtil.set(level, pos, brick(random));
				}
			}
		}

		// Um muro baixo e falhado entre dois cantos.
		Direction wallSide = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		for (int i = -size + 1; i < size; i++) {
			int wx = wallSide.getAxis() == Direction.Axis.X ? size * wallSide.getStepX() : i;
			int wz = wallSide.getAxis() == Direction.Axis.Z ? size * wallSide.getStepZ() : i;
			int height = 1 + random.nextInt(3);
			for (int dy = 1; dy <= height; dy++) {
				if (random.nextFloat() < 0.75F) {
					pos.setWithOffset(base, wx, dy, wz);
					if (FeatureUtil.replaceable(level.getBlockState(pos))) {
						FeatureUtil.set(level, pos, brick(random));
					}
				}
			}
		}

		// Coluna tombada sobre a lama.
		if (random.nextBoolean()) {
			Direction fall = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			BlockPos start = origin.relative(fall, size + 2);
			for (int i = 0; i < 3 + random.nextInt(2); i++) {
				BlockPos log = start.relative(fall, i);
				int top = FeatureUtil.surface(level, log.getX(), log.getZ());
				if (top == Integer.MIN_VALUE || Math.abs(top - origin.getY()) > 2) {
					break;
				}
				FeatureUtil.set(level, new BlockPos(log.getX(), top, log.getZ()), brick(random));
			}
		}
		return true;
	}
}
