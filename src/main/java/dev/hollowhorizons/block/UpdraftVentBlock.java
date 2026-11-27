package dev.hollowhorizons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gêiser de Ar: lança jogadores para cima (ver {@link dev.hollowhorizons.gameplay.Updrafts}).
 * Acima dele, quem plana com as Asas de Seda recupera altitude como numa térmica.
 */
public class UpdraftVentBlock extends Block {
	public static final int RANGE = 10;

	public UpdraftVentBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		for (int i = 0; i < 3; i++) {
			level.addParticle(ParticleTypes.CLOUD,
				pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
				(random.nextDouble() - 0.5) * 0.02, 0.35 + random.nextDouble() * 0.25, (random.nextDouble() - 0.5) * 0.02);
		}
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + random.nextDouble(), pos.getY() + 1.2,
				pos.getZ() + random.nextDouble(), 0.0, 0.2, 0.0);
		}
	}
}
