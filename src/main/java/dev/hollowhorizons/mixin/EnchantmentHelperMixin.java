package dev.hollowhorizons.mixin;

import dev.hollowhorizons.item.VitreousCleaverItem;
import dev.hollowhorizons.registry.HHItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lâmina de Obsidiana Vítrea: reduz a eficácia da armadura do alvo, no mesmo ponto em que o
 * encantamento Brecha (Breach) atua — então os dois se somam de forma previsível.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@Inject(method = "modifyArmorEffectiveness", at = @At("RETURN"), cancellable = true)
	private static void hollowhorizons$vitreousPierce(ServerLevel level, ItemStack weapon, Entity victim, DamageSource source, float armorFraction,
		CallbackInfoReturnable<Float> cir) {
		if (weapon.is(HHItems.VITREOUS_CLEAVER)) {
			cir.setReturnValue(cir.getReturnValueF() * VitreousCleaverItem.ARMOR_EFFECTIVENESS);
		}
	}
}
