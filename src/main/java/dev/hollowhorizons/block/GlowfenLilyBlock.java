package dev.hollowhorizons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Lírio-do-Pântano: planta flutuante bioluminescente da água densa do Pântano do Vazio. */
public class GlowfenLilyBlock extends LilyPadBlock {
	public GlowfenLilyBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(10) == 0) {
			level.addParticle(ParticleTypes.GLOW, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.15,
				pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.02, 0.0);
		}
	}
}
