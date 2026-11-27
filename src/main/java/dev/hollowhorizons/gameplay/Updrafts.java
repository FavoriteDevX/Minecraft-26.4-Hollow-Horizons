package dev.hollowhorizons.gameplay;

import dev.hollowhorizons.block.UpdraftVentBlock;
import dev.hollowhorizons.registry.HHBlocks;
import dev.hollowhorizons.registry.HHItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Física compartilhada (cliente + servidor) dos jogadores: gêiseres de ar e as Asas de Seda.
 * Roda no tick do jogador nos dois lados porque o movimento do jogador é calculado no cliente.
 */
public final class Updrafts {
	/** Velocidade máxima (blocos/tick) das Asas de Seda — a Elytra passa de 3. */
	public static final double SILK_GLIDER_MAX_SPEED = 1.15;
	/** Queda máxima por tick planando com as Asas de Seda: planeio suave. */
	public static final double SILK_GLIDER_MAX_SINK = -0.45;

	private Updrafts() {}

	public static void tick(Player player) {
		if (player.isSpectator()) {
			return;
		}
		Level level = player.level();
		boolean silkGliding = player.isFallFlying() && player.getItemBySlot(EquipmentSlot.CHEST).is(HHItems.SILK_GLIDER);

		int ventDistance = distanceToVent(level, player.blockPosition());
		if (ventDistance > 0 && !player.getAbilities().flying) {
			Vec3 motion = player.getDeltaMovement();
			double strength = 0.12 + 0.05 * (UpdraftVentBlock.RANGE - ventDistance) / UpdraftVentBlock.RANGE;
			double target = silkGliding ? 0.55 : 0.85;
			if (motion.y < target) {
				player.setDeltaMovement(motion.x, Math.min(target, motion.y + strength), motion.z);
			}
			player.resetFallDistance();
		}

		if (silkGliding) {
			Vec3 motion = player.getDeltaMovement();
			double y = Math.max(motion.y, SILK_GLIDER_MAX_SINK);
			Vec3 limited = new Vec3(motion.x, y, motion.z);
			double speed = limited.length();
			if (speed > SILK_GLIDER_MAX_SPEED) {
				limited = limited.scale(SILK_GLIDER_MAX_SPEED / speed);
			}
			if (!limited.equals(motion)) {
				player.setDeltaMovement(limited);
			}
		}
	}

	/** Distância (1..RANGE) até um gêiser diretamente abaixo, com o caminho livre; 0 se não houver. */
	private static int distanceToVent(Level level, BlockPos feet) {
		BlockPos.MutableBlockPos cursor = feet.mutable();
		for (int i = 0; i <= UpdraftVentBlock.RANGE; i++) {
			BlockState state = level.getBlockState(cursor);
			if (state.is(HHBlocks.UPDRAFT_VENT)) {
				return Math.max(1, i);
			}
			if (i > 0 && !state.getCollisionShape(level, cursor).isEmpty()) {
				return 0;
			}
			cursor.move(0, -1, 0);
		}
		return 0;
	}
}
