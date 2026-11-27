package dev.hollowhorizons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Plasma do Vazio: o "líquido" roxo no fundo dos Desfiladeiros de Obsidiana Vítrea.
 * Viscoso como mel, queima como magma e não pode ser coletado — só substituído.
 */
public class VoidPlasmaBlock extends Block {
	public VoidPlasmaBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
		return neighborState.is(this) || super.skipRendering(state, neighborState, direction);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		entity.makeStuckInBlock(state, new Vec3(0.45, 0.35, 0.45));
		if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living && living.tickCount % 10 == 0) {
			living.hurtServer(serverLevel, level.damageSources().magic(), 2.0F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (level.getBlockState(pos.above()).isAir() && random.nextInt(4) == 0) {
			level.addParticle(random.nextBoolean() ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL,
				pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(), 0.0, 0.03, 0.0);
		}
	}
}
