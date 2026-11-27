package dev.hollowhorizons.mixin;

import dev.hollowhorizons.registry.HHItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Asas de Seda são frágeis demais para foguetes: o impulso é recusado. */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void hollowhorizons$noRocketsOnSilk(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player.isFallFlying() && player.getItemBySlot(EquipmentSlot.CHEST).is(HHItems.SILK_GLIDER)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("item.hollowhorizons.silk_glider.no_rockets").withStyle(ChatFormatting.GRAY));
			}
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
