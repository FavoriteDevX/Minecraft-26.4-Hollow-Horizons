package dev.hollowhorizons.item;

import dev.hollowhorizons.mixin.AbstractArrowAccessor;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

/**
 * Arco de Eco: a corda de eco vibra numa frequência que "anula" a gravidade da flecha.
 * Flechas voam em linha reta (sem queda e sem imprecisão) e atravessam até
 * {@value #PIERCE} inimigos. Como não caem, somem após {@value #LIFETIME_TICKS} ticks
 * no ar para não vagarem pelo Vazio para sempre.
 */
public class EchoStringBowItem extends BowItem {
	public static final String ECHO_TAG = "hollowhorizons.echo_arrow";
	public static final int PIERCE = 2;
	public static final int LIFETIME_TICKS = 100;

	public EchoStringBowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float power, float uncertainty, float angle,
		@Nullable LivingEntity targetOverride) {
		projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, power, 0.0F);
		if (projectile instanceof AbstractArrow arrow) {
			arrow.setNoGravity(true);
			((AbstractArrowAccessor) arrow).hollowhorizons$setPierceLevel((byte) Math.max(PIERCE, arrow.getPierceLevel()));
			arrow.addTag(ECHO_TAG);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.hollowhorizons.echo_string_bow.tooltip", PIERCE).withStyle(ChatFormatting.DARK_AQUA));
	}
}
