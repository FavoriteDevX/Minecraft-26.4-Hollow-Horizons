package dev.hollowhorizons.gameplay;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Lógica de servidor que roda todo tick para cada jogador. */
public final class PlayerTickHandler {
	private PlayerTickHandler() {}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			for (ServerPlayer player : level.players()) {
				VoidAnchorHandler.trackGround(player);
				if (VoidAnchorHandler.isFallingIntoVoid(player)) {
					ItemStack anchor = VoidAnchorHandler.findReadyAnchor(player);
					if (anchor != null) {
						VoidAnchorHandler.rescue(player, anchor, false);
					}
				}
				NullStepHandler.tick(player);
			}
		});
	}
}
