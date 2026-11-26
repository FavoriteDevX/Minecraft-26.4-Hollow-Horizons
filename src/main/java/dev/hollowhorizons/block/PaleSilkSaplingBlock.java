package dev.hollowhorizons.block;

import dev.hollowhorizons.registry.HHTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Muda de Seda Pálida: cresce sobre pedra do End e musgo de seda, não precisa de terra. */
public class PaleSilkSaplingBlock extends SaplingBlock {
	public PaleSilkSaplingBlock(TreeGrower grower, BlockBehaviour.Properties properties) {
		super(grower, properties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(HHTags.Blocks.SUPPORTS_END_FLORA) || super.mayPlaceOn(state, level, pos);
	}
}
