package dev.hollowhorizons.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Picareta de Núcleo Oco. A quebra instantânea vem do componente {@code tool} (ver
 * {@link dev.hollowhorizons.registry.HHItems}); a coleta automática — itens vão direto para o
 * inventário em vez de caírem no Vazio — está em {@link dev.hollowhorizons.mixin.BlockMixin}.
 */
public class HollowCorePickaxeItem extends Item {
	public HollowCorePickaxeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.hollowhorizons.hollow_core_pickaxe.tooltip.instant").withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("item.hollowhorizons.hollow_core_pickaxe.tooltip.magnet").withStyle(ChatFormatting.DARK_PURPLE));
	}
}
