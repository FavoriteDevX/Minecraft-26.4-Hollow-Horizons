package dev.hollowhorizons.item;

import dev.hollowhorizons.registry.HHTags;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Lâmina de Obsidiana Vítrea: espada pesada (1,0 ataque/s contra 1,6 da espada comum).
 *
 * <ul>
 *   <li>+{@value #END_BONUS} de dano contra criaturas da tag {@code hollowhorizons:end_creatures}.</li>
 *   <li>Ignora {@value #ARMOR_IGNORED_PERCENT}% da armadura do alvo
 *   ({@link dev.hollowhorizons.mixin.EnchantmentHelperMixin}).</li>
 * </ul>
 */
public class VitreousCleaverItem extends Item {
	public static final float END_BONUS = 4.0F;
	public static final int ARMOR_IGNORED_PERCENT = 40;
	public static final float ARMOR_EFFECTIVENESS = 1.0F - ARMOR_IGNORED_PERCENT / 100.0F;

	public VitreousCleaverItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public float getAttackDamageBonus(Entity victim, float damage, DamageSource damageSource) {
		return victim.is(HHTags.Entities.END_CREATURES) ? END_BONUS : 0.0F;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.hollowhorizons.vitreous_cleaver.tooltip.end", (int) END_BONUS).withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("item.hollowhorizons.vitreous_cleaver.tooltip.armor", ARMOR_IGNORED_PERCENT).withStyle(ChatFormatting.DARK_PURPLE));
	}
}
