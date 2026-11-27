package dev.hollowhorizons.gameplay;

import dev.hollowhorizons.item.VoidAnchorItem;
import dev.hollowhorizons.registry.HHAttachments;
import dev.hollowhorizons.registry.HHComponents;
import dev.hollowhorizons.registry.HHItems;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Lógica da Âncora do Vazio: memória do último chão firme e o puxão de resgate. */
public final class VoidAnchorHandler {
	/** Quantos blocos abaixo do fundo do mundo a âncora espera antes de puxar (bem antes do dano do Vazio). */
	public static final int TRIGGER_DEPTH = 6;

	private VoidAnchorHandler() {}

	/** Chamado a cada tick do servidor: memoriza o bloco sólido sob os pés do jogador. */
	public static void trackGround(ServerPlayer player) {
		if (!player.onGround() || player.isSpectator() || player.isPassenger()) {
			return;
		}
		ServerLevel level = player.level();
		BlockPos below = player.getBlockPosBelowThatAffectsMyMovement();
		BlockState state = level.getBlockState(below);
		if (!state.isFaceSturdy(level, below, Direction.UP)) {
			return;
		}
		GlobalPos current = player.getAttached(HHAttachments.LAST_SAFE_GROUND);
		GlobalPos next = GlobalPos.of(level.dimension(), below);
		if (!next.equals(current)) {
			player.setAttached(HHAttachments.LAST_SAFE_GROUND, next);
		}
	}

	/** O jogador está caindo no Vazio? */
	public static boolean isFallingIntoVoid(ServerPlayer player) {
		return !player.isSpectator() && !player.getAbilities().flying
			&& player.getY() < player.level().getMinY() - TRIGGER_DEPTH && player.getDeltaMovement().y < 0;
	}

	/** Primeira âncora com carga e fora de recarga no inventário (mão secundária primeiro). */
	public static @Nullable ItemStack findReadyAnchor(ServerPlayer player) {
		ItemStack offhand = player.getOffhandItem();
		if (isReady(player, offhand)) {
			return offhand;
		}
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (isReady(player, stack)) {
				return stack;
			}
		}
		return null;
	}

	private static boolean isReady(ServerPlayer player, ItemStack stack) {
		return stack.is(HHItems.VOID_ANCHOR) && VoidAnchorItem.charges(stack) > 0 && !player.getCooldowns().isOnCooldown(stack);
	}

	/**
	 * Puxa o jogador de volta ao último chão firme.
	 *
	 * @param manual verdadeiro quando o jogador arremessou a âncora de propósito (agachar + usar)
	 * @return se o puxão aconteceu
	 */
	public static boolean rescue(ServerPlayer player, ItemStack anchor, boolean manual) {
		if (!anchor.is(HHItems.VOID_ANCHOR)) {
			return false;
		}
		if (player.getCooldowns().isOnCooldown(anchor)) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.void_anchor.cooldown").withStyle(ChatFormatting.GRAY));
			return false;
		}
		int charges = VoidAnchorItem.charges(anchor);
		if (charges <= 0 && !player.hasInfiniteMaterials()) {
			player.sendOverlayMessage(Component.translatable("item.hollowhorizons.void_anchor.empty").withStyle(ChatFormatting.RED));
			return false;
		}

		ServerLevel level = player.level();
		Vec3 destination = safeDestination(player, level);
		Vec3 origin = player.position();

		drawChain(level, origin, destination);
		level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);

		player.teleportTo(level, destination.x, destination.y, destination.z, Set.of(), player.getYRot(), player.getXRot(), false);
		player.setDeltaMovement(Vec3.ZERO);
		player.needsSync = true;
		player.resetFallDistance();
		player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, false, true));

		level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 1.0, destination.z, 60, 0.4, 0.8, 0.4, 0.05);

		if (!player.hasInfiniteMaterials()) {
			anchor.set(HHComponents.ANCHOR_CHARGES, Math.max(0, charges - 1));
		}
		player.getCooldowns().addCooldown(anchor, VoidAnchorItem.COOLDOWN_TICKS);
		player.sendOverlayMessage(Component.translatable(manual
			? "item.hollowhorizons.void_anchor.recalled" : "item.hollowhorizons.void_anchor.saved").withStyle(ChatFormatting.LIGHT_PURPLE));
		return true;
	}

	/** Centro do topo do último bloco sólido conhecido nesta dimensão; senão, o chão mais alto mais próximo. */
	private static Vec3 safeDestination(ServerPlayer player, ServerLevel level) {
		GlobalPos remembered = player.getAttached(HHAttachments.LAST_SAFE_GROUND);
		if (remembered != null && remembered.dimension() == level.dimension()
			&& level.getBlockState(remembered.pos()).isFaceSturdy(level, remembered.pos(), Direction.UP)) {
			BlockPos pos = remembered.pos();
			return new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
		}
		// Sem memória válida: no End volta para a ilha central; nas outras dimensões, para o spawn.
		BlockPos fallback = level.dimension() == Level.END ? BlockPos.ZERO : level.getRespawnData().pos();
		BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fallback);
		if (top.getY() <= level.getMinY()) {
			top = new BlockPos(fallback.getX(), 64, fallback.getZ());
		}
		return Vec3.atBottomCenterOf(top);
	}

	/** A "corrente de energia": um rastro de partículas entre o ponto de queda e a âncora. */
	private static void drawChain(ServerLevel level, Vec3 from, Vec3 to) {
		Vec3 delta = to.subtract(from);
		int links = (int) Math.min(64, Math.max(8, delta.length() / 2.0));
		for (int i = 0; i <= links; i++) {
			Vec3 point = from.add(delta.scale((double) i / links));
			level.sendParticles(ParticleTypes.PORTAL, point.x, point.y + 0.5, point.z, 2, 0.05, 0.05, 0.05, 0.0);
		}
	}
}
