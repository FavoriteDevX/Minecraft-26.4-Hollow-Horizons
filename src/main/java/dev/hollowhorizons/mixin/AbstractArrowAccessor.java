package dev.hollowhorizons.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Dá acesso ao nível de perfuração (privado no vanilla) para o Arco de Eco. */
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
	@Invoker("setPierceLevel")
	void hollowhorizons$setPierceLevel(byte level);
}
