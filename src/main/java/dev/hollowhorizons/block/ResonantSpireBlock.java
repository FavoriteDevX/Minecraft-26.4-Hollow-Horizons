package dev.hollowhorizons.block;

import dev.hollowhorizons.gameplay.Resonance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pilar de Quartzo Ressonante. Cada bloco "afina" numa nota da escala pentatônica
 * conforme sua posição, então caminhar sobre os Campos produz acordes, não ruído.
 */
public class ResonantSpireBlock extends RotatedPillarBlock {
	public ResonantSpireBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState onState, Entity entity) {
		if (!level.isClientSide() && !entity.isSteppingCarefully() && entity.tickCount % 6 == 0) {
			Resonance.chime(level, pos, 0.55F);
		}
		super.stepOn(level, pos, onState, entity);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		// "Quando o vento passa": pilares expostos ao vazio cantam sozinhos, raramente.
		if (random.nextInt(260) == 0 && level.getBlockState(pos.above()).isAir()) {
			Resonance.windChime(level, pos, random);
		}
	}
}
