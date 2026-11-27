package dev.hollowhorizons.network;

import dev.hollowhorizons.gameplay.NullStepHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class HHNetworking {
	private HHNetworking() {}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(DoubleJumpPayload.TYPE, DoubleJumpPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DoubleJumpPayload.TYPE,
			(payload, context) -> NullStepHandler.onDoubleJump(context.player()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> NullStepHandler.forget(handler.getPlayer().getUUID()));
	}
}
