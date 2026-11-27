package dev.hollowhorizons.item;

import dev.hollowhorizons.gameplay.VoidAnchorHandler;
import dev.hollowhorizons.registry.HHComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Âncora do Vazio.
 *
 * <ul>
 *   <li><b>Passivo:</b> se o portador cair no Vazio com uma carga disponível, a âncora é
 *   "arremessada" pela corrente de energia até o último bloco sólido onde ele pisou e o puxa
 *   de volta ({@link VoidAnchorHandler}).</li>
 *   <li><b>Usar:</b> consome uma Pérola do Ender do inventário para recarregar (+1 carga).</li>
 *   <li><b>Agachar + usar:</b> arremesso manual — volta ao último chão firme agora.</li>
 *   <li>Cada puxão gasta 1 carga e deixa a âncora em recarga por 30 segundos.</li>
 * </ul>
 */
public class VoidAnchorItem extends Item {
	public static final int MAX_CHARGES = 4;
	public static final int COOLDOWN_TICKS = 30 * 20;

	public VoidAnchorItem(Item.Properties properties) {
		super(properties);
	}

	public static int charges(ItemStack stack) {
		return stack.getOrDefault(HHComponents.ANCHOR_CHARGES, 0);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.isShiftKeyDown()) {
			if (player instanceof ServerPlayer serverPlayer) {
				return VoidAnchorHandler.rescue(serverPlayer, stack, true) ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
			}
			return InteractionResult.SUCCESS;
		}

		int charges = charges(stack);
		if (charges >= MAX_CHARGES) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.void_anchor.full").withStyle(ChatFormatting.LIGHT_PURPLE));
			return InteractionResult.FAIL;
		}

		int pearlSlot = findPearl(player);
		if (pearlSlot < 0 && !player.hasInfiniteMaterials()) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.void_anchor.need_pearl").withStyle(ChatFormatting.RED));
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			if (pearlSlot >= 0 && !player.hasInfiniteMaterials()) {
				player.getInventory().getItem(pearlSlot).shrink(1);
			}
			stack.set(HHComponents.ANCHOR_CHARGES, charges + 1);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.8F,
				0.9F + 0.1F * charges);
		}
		return InteractionResult.SUCCESS;
	}

	private static int findPearl(Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (player.getInventory().getItem(i).is(Items.ENDER_PEARL)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * charges(stack) / MAX_CHARGES);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0xB45CFF;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.hollowhorizons.void_anchor.charges", charges(stack), MAX_CHARGES).withStyle(ChatFormatting.LIGHT_PURPLE));
		builder.accept(Component.translatable("item.hollowhorizons.void_anchor.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
