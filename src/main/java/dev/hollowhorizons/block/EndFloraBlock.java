package dev.hollowhorizons.block;

import dev.hollowhorizons.registry.HHTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Planta simples do End (Junco do Vazio): nasce em lama do vazio, musgo de seda e pedra do End. */
public class EndFloraBlock extends VegetationBlock {
	private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

	public EndFloraBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(HHTags.Blocks.SUPPORTS_END_FLORA);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
