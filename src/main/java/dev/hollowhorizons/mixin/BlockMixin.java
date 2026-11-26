package dev.hollowhorizons.mixin;

import dev.hollowhorizons.registry.HHItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Picareta de Núcleo Oco: os drops vão direto para o inventário de quem minerou, em vez de
 * virarem itens soltos que rolariam para o Vazio. O que não couber cai aos pés do jogador.
 */
@Mixin(Block.class)
public abstract class BlockMixin {
	@Inject(
		method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At("HEAD"), cancellable = true)
	private static void hollowhorizons$hollowCoreMagnet(BlockState state, Level level, BlockPos pos, @Nullable BlockEntity blockEntity,
		@Nullable Entity breaker, ItemStack tool, CallbackInfo ci) {
		if (!(level instanceof ServerLevel serverLevel) || !(breaker instanceof Player player) || !tool.is(HHItems.HOLLOW_CORE_PICKAXE)) {
			return;
		}
		boolean dropsEnabled = serverLevel.getGameRules().get(GameRules.BLOCK_DROPS);
		if (dropsEnabled) {
			for (ItemStack drop : Block.getDrops(state, serverLevel, pos, blockEntity, breaker, tool)) {
				if (!player.getInventory().add(drop) && !drop.isEmpty()) {
					Block.popResource(serverLevel, player.blockPosition(), drop);
				}
			}
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
				1.4F + serverLevel.getRandom().nextFloat() * 0.4F);
		}
		state.spawnAfterBreak(serverLevel, pos, tool, true);
		ci.cancel();
	}
}
