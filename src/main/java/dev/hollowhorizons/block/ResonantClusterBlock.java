package dev.hollowhorizons.block;

import dev.hollowhorizons.gameplay.Resonance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Aglomerado de cristal ressonante (e de vidro vítreo) — fonte dos fragmentos. */
public class ResonantClusterBlock extends AmethystClusterBlock {
	private final boolean sings;

	public ResonantClusterBlock(float height, float width, boolean sings, BlockBehaviour.Properties properties) {
		super(height, width, properties);
		this.sings = sings;
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState onState, Entity entity) {
		if (this.sings && !level.isClientSide() && entity.tickCount % 8 == 0) {
			Resonance.chime(level, pos, 0.4F);
		}
		super.stepOn(level, pos, onState, entity);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!this.sings) {
			return;
		}
		if (random.nextInt(12) == 0) {
			level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 0.6 + random.nextDouble() * 0.4,
				pos.getZ() + random.nextDouble(), 0.0, 0.01, 0.0);
		}
		if (random.nextInt(320) == 0) {
			Resonance.windChime(level, pos, random);
		}
	}
}
