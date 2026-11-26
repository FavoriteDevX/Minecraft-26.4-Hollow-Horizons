package dev.hollowhorizons.world.feature;

import dev.hollowhorizons.registry.HHTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Utilidades comuns às features do End. */
final class FeatureUtil {
	private FeatureUtil() {}

	/** Pode ser sobrescrito por uma estrutura natural (ar, plantas ou chão do End). */
	static boolean replaceable(BlockState state) {
		return state.isAir() || state.canBeReplaced() || state.is(HHTags.Blocks.END_GROUND) || state.is(BlockTags.LEAVES);
	}

	static boolean isGround(BlockState state) {
		return state.is(HHTags.Blocks.END_GROUND);
	}

	/** Altura da superfície (primeiro bloco livre) na coluna, ou {@code Integer.MIN_VALUE} se for vazio. */
	static int surface(WorldGenLevel level, int x, int z) {
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		return y <= level.getMinY() + 1 ? Integer.MIN_VALUE : y;
	}

	static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
		if (level.ensureCanWrite(pos)) {
			level.setBlock(pos, state, 2);
		}
	}
}
