package dev.hollowhorizons.mixin;

import dev.hollowhorizons.item.EchoStringBowItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Flechas do Arco de Eco: rastro sônico e vida limitada enquanto voam sem gravidade. */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Shadow
	protected abstract boolean isInGround();

	@Inject(method = "tick", at = @At("TAIL"))
	private void hollowhorizons$echoArrowTick(CallbackInfo ci) {
		AbstractArrow self = (AbstractArrow) (Object) this;
		if (!(self.level() instanceof ServerLevel level) || !self.entityTags().contains(EchoStringBowItem.ECHO_TAG)) {
			return;
		}
		if (this.isInGround()) {
			self.setNoGravity(false);
			return;
		}
		if (self.tickCount % 2 == 0) {
			level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, self.getX(), self.getY(), self.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		}
		if (self.tickCount > EchoStringBowItem.LIFETIME_TICKS) {
			level.sendParticles(ParticleTypes.SONIC_BOOM, self.getX(), self.getY(), self.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
			self.discard();
		}
	}
}
