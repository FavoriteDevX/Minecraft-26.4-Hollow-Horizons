package dev.hollowhorizons.client;

import dev.hollowhorizons.gameplay.NullStepHandler;
import dev.hollowhorizons.network.DoubleJumpPayload;
import dev.hollowhorizons.registry.HHParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public final class HollowHorizonsClient implements ClientModInitializer {
	private static boolean jumpWasDown;
	private static boolean usedDoubleJump;

	@Override
	public void onInitializeClient() {
		ParticleProviderRegistry.getInstance().register(HHParticles.SILK_SPORE, SilkSporeParticle.Provider::new);
		ClientTickEvents.END_CLIENT_TICK.register(HollowHorizonsClient::tickDoubleJump);
	}

	/**
	 * Salto duplo das Botas de Gravidade Zero: um segundo toque em "pular" no ar.
	 * O impulso é aplicado no cliente (dono do movimento do jogador) e o servidor é avisado
	 * para validar e zerar a distância de queda.
	 */
	private static void tickDoubleJump(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			jumpWasDown = false;
			return;
		}
		boolean jumpDown = player.input.keyPresses.jump();
		if (player.onGround() || player.isInWater() || player.onClimbable() || player.getAbilities().flying) {
			usedDoubleJump = false;
		} else if (jumpDown && !jumpWasDown && !usedDoubleJump && NullStepHandler.canDoubleJump(player)) {
			Vec3 motion = player.getDeltaMovement();
			player.setDeltaMovement(motion.x * 1.15, NullStepHandler.DOUBLE_JUMP_VELOCITY, motion.z * 1.15);
			player.resetFallDistance();
			usedDoubleJump = true;
			ClientPlayNetworking.send(DoubleJumpPayload.INSTANCE);
			for (int i = 0; i < 12; i++) {
				double angle = i / 12.0 * Math.PI * 2.0;
				player.level().addParticle(ParticleTypes.REVERSE_PORTAL, player.getX() + Math.cos(angle) * 0.5, player.getY(),
					player.getZ() + Math.sin(angle) * 0.5, Math.cos(angle) * 0.05, -0.05, Math.sin(angle) * 0.05);
			}
		}
		jumpWasDown = jumpDown;
	}
}
