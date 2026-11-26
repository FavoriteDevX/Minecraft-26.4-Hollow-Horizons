package dev.hollowhorizons.gameplay;

import dev.hollowhorizons.HollowHorizons;
import dev.hollowhorizons.registry.HHItems;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Botas de Gravidade Zero.
 *
 * <ul>
 *   <li><b>Salto duplo:</b> o cliente detecta o segundo pulo no ar, aplica o impulso
 *   localmente e avisa o servidor, que valida e zera a distância de queda.</li>
 *   <li><b>Queda amortecida:</b> em qualquer lugar +3 blocos de queda segura; nas ilhas do
 *   End o dano de queda cai {@value #END_FALL_REDUCTION_PERCENT}% e a queda segura sobe para +8.</li>
 * </ul>
 */
public final class NullStepHandler {
	public static final double DOUBLE_JUMP_VELOCITY = 0.62;
	public static final int END_FALL_REDUCTION_PERCENT = 80;

	private static final Identifier SAFE_FALL_ID = HollowHorizons.id("null_step_safe_fall");
	private static final Identifier FALL_DAMAGE_ID = HollowHorizons.id("null_step_fall_damage");
	private static final Set<UUID> USED_DOUBLE_JUMP = new HashSet<>();

	private NullStepHandler() {}

	public static void init() {}

	public static boolean wearing(Player player) {
		return player.getItemBySlot(EquipmentSlot.FEET).is(HHItems.NULL_STEP_BOOTS);
	}

	/** Regras comuns ao cliente e ao servidor para permitir o segundo pulo. */
	public static boolean canDoubleJump(Player player) {
		return wearing(player) && !player.onGround() && !player.isFallFlying() && !player.getAbilities().flying
			&& !player.isInWater() && !player.isInLava() && !player.isPassenger() && !player.onClimbable();
	}

	public static void tick(ServerPlayer player) {
		if (player.onGround()) {
			USED_DOUBLE_JUMP.remove(player.getUUID());
		}

		boolean wearing = wearing(player);
		boolean inEnd = player.level().dimension() == Level.END;
		AttributeInstance safeFall = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
		AttributeInstance fallDamage = player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
		if (safeFall == null || fallDamage == null) {
			return;
		}
		if (wearing) {
			safeFall.addOrUpdateTransientModifier(new AttributeModifier(SAFE_FALL_ID, inEnd ? 8.0 : 3.0, AttributeModifier.Operation.ADD_VALUE));
			if (inEnd) {
				fallDamage.addOrUpdateTransientModifier(new AttributeModifier(FALL_DAMAGE_ID, -END_FALL_REDUCTION_PERCENT / 100.0,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			} else {
				fallDamage.removeModifier(FALL_DAMAGE_ID);
			}
		} else {
			safeFall.removeModifier(SAFE_FALL_ID);
			fallDamage.removeModifier(FALL_DAMAGE_ID);
		}
	}

	/** Recebido do cliente quando ele executou o salto duplo. */
	public static void onDoubleJump(ServerPlayer player) {
		if (!canDoubleJump(player) || !USED_DOUBLE_JUMP.add(player.getUUID())) {
			return;
		}
		player.resetFallDistance();
		player.level().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY(), player.getZ(), 24, 0.3, 0.05, 0.3, 0.02);
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.7F, 1.4F);
	}

	public static void forget(UUID player) {
		USED_DOUBLE_JUMP.remove(player);
	}
}
