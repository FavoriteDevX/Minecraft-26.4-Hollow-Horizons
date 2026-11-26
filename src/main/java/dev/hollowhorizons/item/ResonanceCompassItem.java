package dev.hollowhorizons.item;

import dev.hollowhorizons.registry.HHComponents;
import dev.hollowhorizons.registry.HHItems;
import dev.hollowhorizons.registry.HHTags;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Bússola de Ressonância: "afina" fragmentos de cristal ressonante na frequência de uma
 * estrutura rara do End e passa a apontar para a mais próxima.
 *
 * <ul>
 *   <li><b>Usar:</b> consome 1 Fragmento Ressonante e procura o alvo atual (raio de 100 chunks).</li>
 *   <li><b>Agachar + usar:</b> troca o alvo (Cidade do End e seus Navios ↔ Ruínas do Vazio).</li>
 * </ul>
 *
 * A agulha usa o componente vanilla {@code lodestone_tracker} (sem exigir magnetita), por
 * isso o modelo do item reaproveita a propriedade {@code minecraft:compass}.
 */
public class ResonanceCompassItem extends Item {
	public static final int SEARCH_RADIUS_CHUNKS = 100;

	public enum Target {
		END_CITY("end_city", HHTags.Structures.RESONANCE_END_CITY),
		VOID_RUINS("void_ruins", HHTags.Structures.RESONANCE_VOID_RUINS);

		public final String key;
		public final TagKey<Structure> structures;

		Target(String key, TagKey<Structure> structures) {
			this.key = key;
			this.structures = structures;
		}

		public static Target of(ItemStack stack) {
			int index = stack.getOrDefault(HHComponents.RESONANCE_TARGET, 0);
			return values()[Math.floorMod(index, values().length)];
		}

		public Component label() {
			return Component.translatable("item.hollowhorizons.resonance_compass.target." + this.key);
		}
	}

	public ResonanceCompassItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Target target = Target.of(stack);

		if (player.isShiftKeyDown()) {
			Target next = Target.values()[(target.ordinal() + 1) % Target.values().length];
			stack.set(HHComponents.RESONANCE_TARGET, next.ordinal());
			stack.remove(DataComponents.LODESTONE_TRACKER);
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.resonance_compass.tuned", next.label()).withStyle(ChatFormatting.AQUA));
			level.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.8F + next.ordinal() * 0.4F);
			return InteractionResult.SUCCESS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (level.dimension() != Level.END) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.resonance_compass.wrong_dimension").withStyle(ChatFormatting.GRAY));
			return InteractionResult.FAIL;
		}
		if (!player.hasInfiniteMaterials() && !consumeShard(player)) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.resonance_compass.need_shard").withStyle(ChatFormatting.RED));
			return InteractionResult.FAIL;
		}

		BlockPos found = serverLevel.findNearestMapStructure(target.structures, player.blockPosition(), SEARCH_RADIUS_CHUNKS, false);
		if (found == null) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.resonance_compass.silent", target.label()).withStyle(ChatFormatting.GRAY));
			serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.8F, 0.6F);
			return InteractionResult.FAIL;
		}

		stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), found)), false));
		int distance = (int) Math.sqrt(player.blockPosition().distSqr(new BlockPos(found.getX(), player.getBlockY(), found.getZ())));
		player.sendOverlayMessage(Component.translatable("item.hollowhorizons.resonance_compass.found", target.label(), distance).withStyle(ChatFormatting.LIGHT_PURPLE));
		serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
		player.getCooldowns().addCooldown(stack, 40);
		return InteractionResult.SUCCESS_SERVER;
	}

	private static boolean consumeShard(Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack candidate = player.getInventory().getItem(i);
			if (candidate.is(HHItems.RESONANT_SHARD)) {
				candidate.shrink(1);
				return true;
			}
		}
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.hollowhorizons.resonance_compass.current", Target.of(stack).label()).withStyle(ChatFormatting.AQUA));
		builder.accept(Component.translatable("item.hollowhorizons.resonance_compass.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
