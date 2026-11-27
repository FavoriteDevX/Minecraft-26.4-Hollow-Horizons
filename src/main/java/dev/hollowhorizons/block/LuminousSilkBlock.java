package dev.hollowhorizons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import dev.hollowhorizons.registry.HHParticles;

/**
 * Teia de Luz: fios que pendem das copas da Seda Pálida. Brilha no escuro e prende
 * bem menos que uma teia comum — é bonita, não uma armadilha.
 */
public class LuminousSilkBlock extends WebBlock {
	public LuminousSilkBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		entity.makeStuckInBlock(state, new Vec3(0.7, 0.55, 0.7));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(6) == 0) {
			level.addParticle(HHParticles.SILK_SPORE,
				pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
				0.0, -0.01, 0.0);
		}
	}
}
