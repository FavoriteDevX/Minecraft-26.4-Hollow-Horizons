package dev.hollowhorizons.test;

import dev.hollowhorizons.HollowHorizons;
import dev.hollowhorizons.gameplay.NullStepHandler;
import dev.hollowhorizons.gameplay.Updrafts;
import dev.hollowhorizons.gameplay.VoidAnchorHandler;
import dev.hollowhorizons.item.EchoStringBowItem;
import dev.hollowhorizons.item.VitreousCleaverItem;
import dev.hollowhorizons.item.VoidAnchorItem;
import dev.hollowhorizons.registry.HHAttachments;
import dev.hollowhorizons.registry.HHBlocks;
import dev.hollowhorizons.registry.HHComponents;
import dev.hollowhorizons.registry.HHItems;
import dev.hollowhorizons.registry.HHWorldgen;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import dev.hollowhorizons.world.structure.VoidRuinsPiece;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Testes que rodam dentro de um servidor Minecraft de verdade ({@code ./gradlew runGameTest}).
 * Cada teste exercita uma promessa do README: se um falhar, o build falha.
 */
public class HollowHorizonsGameTests {
	private static ServerPlayer player(GameTestHelper helper, BlockPos relative) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Vec3 pos = Vec3.atBottomCenterOf(helper.absolutePos(relative));
		player.snapTo(pos.x, pos.y, pos.z);
		return player;
	}

	private static void done(GameTestHelper helper, ServerPlayer... players) {
		for (ServerPlayer player : players) {
			helper.getLevel().getServer().getPlayerList().remove(player);
		}
		helper.succeed();
	}

	// ------------------------------------------------------------------ registros e worldgen

	@GameTest
	public void registriesContainTheExpansion(GameTestHelper helper) {
		var access = helper.getLevel().registryAccess();
		for (ResourceKey<Biome> biome : HHWorldgen.ALL_BIOMES) {
			helper.assertTrue(access.lookupOrThrow(Registries.BIOME).get(biome).isPresent(), "Bioma ausente: " + biome.identifier());
		}
		for (String id : List.of("pale_silk_tree", "chasm_rift", "plasma_pool", "vitreous_spire", "resonant_spire", "fen_ruin", "fen_pool",
			"glowfen_lily", "voidreed", "updraft_vent", "resonant_cluster", "vitreous_shard_cluster", "luminous_silk_patch")) {
			helper.assertTrue(access.lookupOrThrow(Registries.FEATURE).get(ResourceKey.create(Registries.FEATURE, HollowHorizons.id(id))).isPresent(),
				"Feature ausente: " + id);
		}
		for (String id : List.of("pale_silk_trees", "luminous_silk_patch", "chasm_rift", "plasma_pool", "vitreous_spire", "updraft_vent",
			"vitreous_shard_cluster", "resonant_spires", "resonant_cluster_patch", "fen_pools", "glowfen_lily_patch", "voidreed_patch", "fen_ruin")) {
			helper.assertTrue(access.lookupOrThrow(Registries.PLACED_FEATURE).get(ResourceKey.create(Registries.PLACED_FEATURE, HollowHorizons.id(id))).isPresent(),
				"Placed feature ausente: " + id);
		}
		helper.assertTrue(access.lookupOrThrow(Registries.STRUCTURE).get(ResourceKey.create(Registries.STRUCTURE, HollowHorizons.id("void_ruins"))).isPresent(),
			"Estrutura void_ruins ausente");
		helper.succeed();
	}

	/**
	 * O servidor de GameTest usa um End plano ({@code flat_all_dimensions}), então este teste
	 * monta o gerador de biomas REAL do End (o mesmo de um mundo normal, com a injeção do
	 * Fabric) e varre as ilhas externas. A localização das estruturas num mundo de verdade é
	 * verificada por {@code tools/smoke_test.py}.
	 */
	@GameTest
	public void theFourBiomesGenerateInTheOuterEnd(GameTestHelper helper) {
		var access = helper.getLevel().registryAccess();
		TheEndBiomeSource source = TheEndBiomeSource.create(access.lookupOrThrow(Registries.BIOME));
		NoiseGeneratorSettings settings = access.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.END).value();
		RandomState random = RandomState.create(access.lookupOrThrow(Registries.NOISE), 26042026L, settings);
		BiomeResolver resolver = source.createUncachedResolver(random);

		for (ResourceKey<Biome> biome : HHWorldgen.ALL_BIOMES) {
			helper.assertTrue(source.possibleBiomes().stream().anyMatch(h -> h.is(biome)), "Fonte do End não conhece " + biome.identifier());
		}
		Map<ResourceKey<Biome>, Integer> counts = new HashMap<>();
		for (int x = -6000; x <= 6000; x += 48) {
			for (int z = -6000; z <= 6000; z += 48) {
				resolver.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(64), QuartPos.fromBlock(z))
					.unwrapKey().ifPresent(key -> counts.merge(key, 1, Integer::sum));
			}
		}
		HollowHorizons.LOGGER.info("[gametest] Amostra de biomas do End: {}", counts);
		// Proporção entre as "terras altas": nenhum bioma novo pode ficar raro demais.
		int highlands = counts.getOrDefault(Biomes.END_HIGHLANDS, 0);
		for (ResourceKey<Biome> biome : HHWorldgen.ALL_BIOMES) {
			highlands += counts.getOrDefault(biome, 0);
		}
		for (ResourceKey<Biome> biome : HHWorldgen.ALL_BIOMES) {
			double share = counts.getOrDefault(biome, 0) / (double) Math.max(1, highlands);
			helper.assertTrue(share >= 0.08, "Bioma raro demais nas ilhas externas: " + biome.identifier() + " (" + Math.round(share * 100) + "%)");
		}
		helper.assertTrue(counts.getOrDefault(Biomes.END_HIGHLANDS, 0) > 0, "As terras altas vanilla continuam existindo");
		helper.assertTrue(resolver.getNoiseBiome(0, 16, 0).is(Biomes.THE_END), "A ilha central (dragão) continua intacta");
		helper.succeed();
	}

	@GameTest(padding = 24, maxTicks = 100)
	public void voidRuinsBuildTempleWithChest(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos start = helper.absolutePos(new BlockPos(0, 1, 0));
		VoidRuinsPiece piece = new VoidRuinsPiece(level.getRandom(), start);
		piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
			piece.getBoundingBox(), ChunkPos.containing(start), start);
		BlockPos altar = new BlockPos(piece.getBoundingBox().minX() + VoidRuinsPiece.WIDTH / 2, start.getY() + 3,
			piece.getBoundingBox().minZ() + VoidRuinsPiece.DEPTH / 2);
		helper.assertTrue(level.getBlockState(altar).is(Blocks.CHEST), "O altar deveria ter um baú (achou " + level.getBlockState(altar) + ")");
		helper.assertTrue(level.getBlockState(altar.below()).is(HHBlocks.VITREOUS_OBSIDIAN), "Pedestal de Obsidiana Vítrea sob o baú");
		helper.succeed();
	}

	@GameTest(padding = 24, maxTicks = 100)
	public void customFeaturesBuildTheirShapes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				for (int y = 0; y < 5; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.END_STONE);
				}
			}
		}
		var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
		var random = level.getRandom();
		var generator = level.getChunkSource().getGenerator();
		BlockPos top = helper.absolutePos(new BlockPos(3, 5, 3));

		helper.assertTrue(features.getOrThrow(key("resonant_spire")).value().place(level, generator, random, top), "resonant_spire não foi colocada");
		helper.assertTrue(level.getBlockState(top).is(HHBlocks.RESONANT_SPIRE), "A torre ressonante deveria começar no chão");

		BlockPos corner = helper.absolutePos(new BlockPos(6, 5, 6));
		helper.assertTrue(features.getOrThrow(key("vitreous_spire")).value().place(level, generator, random, corner), "vitreous_spire não foi colocada");
		helper.assertTrue(level.getBlockState(corner).is(HHBlocks.VITREOUS_OBSIDIAN), "O espinho vítreo deveria ser de Obsidiana Vítrea");
		helper.succeed();
	}

	private static ResourceKey<Feature> key(String id) {
		return ResourceKey.create(Registries.FEATURE, HollowHorizons.id(id));
	}

	// ------------------------------------------------------------------ itens

	@GameTest
	public void hollowCorePickaxeIsInstantAndMagnetic(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ItemStack pickaxe = new ItemStack(HHItems.HOLLOW_CORE_PICKAXE);
		BlockState endStone = Blocks.END_STONE.defaultBlockState();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.END_STONE);

		float speed = pickaxe.getDestroySpeed(endStone);
		float perTick = speed / endStone.getDestroySpeed(level, pos) / 30.0F;
		helper.assertTrue(perTick >= 1.0F, "Pedra do End deveria quebrar em 1 tick (progresso por tick = " + perTick + ")");
		helper.assertTrue(pickaxe.isCorrectToolForDrops(endStone), "A picareta deveria coletar pedra do End");
		helper.assertTrue(pickaxe.getDestroySpeed(Blocks.OBSIDIAN.defaultBlockState()) < 1000.0F, "Obsidiana comum não é bloco do End");

		ServerPlayer player = player(helper, new BlockPos(2, 1, 2));
		Block.dropResources(endStone, level, pos, null, player, pickaxe);
		helper.assertTrue(player.getInventory().contains(new ItemStack(Items.END_STONE)), "O drop deveria ir direto para o inventário");
		helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).isEmpty(), "Nenhum item deveria ficar solto no chão");
		done(helper, player);
	}

	@GameTest(maxTicks = 40)
	public void voidAnchorPullsFallingPlayerBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(new BlockPos(2, 1, 2), Blocks.END_STONE);
		BlockPos safe = helper.absolutePos(new BlockPos(2, 1, 2));
		ServerPlayer player = player(helper, new BlockPos(2, 2, 2));
		player.setAttached(HHAttachments.LAST_SAFE_GROUND, GlobalPos.of(level.dimension(), safe));
		player.setItemSlot(EquipmentSlot.OFFHAND, HHItems.charged(HHItems.VOID_ANCHOR, 2));

		player.teleportTo(level, safe.getX() + 0.5, level.getMinY() - 20, safe.getZ() + 0.5, Set.of(), 0, 0, false);
		player.setDeltaMovement(0, -1.5, 0);
		helper.assertTrue(VoidAnchorHandler.isFallingIntoVoid(player), "Deveria detectar a queda no Vazio");

		// Quem puxa é o tick do servidor (PlayerTickHandler), não o teste.
		helper.succeedWhen(() -> {
			helper.assertTrue(player.getY() >= safe.getY() + 1 - 0.01, "A âncora não puxou o jogador (y=" + player.getY() + ")");
			helper.assertTrue(player.getCooldowns().isOnCooldown(player.getOffhandItem()), "A âncora deveria entrar em recarga de 30 s");
			helper.getLevel().getServer().getPlayerList().remove(player);
		});
	}

	@GameTest
	public void voidAnchorRechargesWithEnderPearls(GameTestHelper helper) {
		ServerPlayer player = player(helper, new BlockPos(1, 1, 1));
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		ItemStack anchor = HHItems.charged(HHItems.VOID_ANCHOR, 0);
		player.setItemInHand(InteractionHand.MAIN_HAND, anchor);
		player.getInventory().add(new ItemStack(Items.ENDER_PEARL, 2));
		HHItems.VOID_ANCHOR.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(VoidAnchorItem.charges(player.getMainHandItem()) == 1, "Uma pérola deveria virar uma carga");
		helper.assertTrue(player.getInventory().countItem(Items.ENDER_PEARL) == 1, "A pérola deveria ser consumida");
		done(helper, player);
	}

	@GameTest
	public void vitreousCleaverPunishesTheEnd(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Enderman enderman = helper.spawn(EntityTypes.ENDERMAN, new BlockPos(1, 1, 1));
		Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 1, 3));
		DamageSource source = level.damageSources().generic();
		ItemStack cleaver = new ItemStack(HHItems.VITREOUS_CLEAVER);

		helper.assertTrue(HHItems.VITREOUS_CLEAVER.getAttackDamageBonus(enderman, 9.0F, source) == VitreousCleaverItem.END_BONUS,
			"Dano extra contra enderman");
		helper.assertTrue(HHItems.VITREOUS_CLEAVER.getAttackDamageBonus(zombie, 9.0F, source) == 0.0F, "Sem bônus contra zumbi");
		float pierced = EnchantmentHelper.modifyArmorEffectiveness(level, cleaver, zombie, source, 1.0F);
		helper.assertTrue(Math.abs(pierced - VitreousCleaverItem.ARMOR_EFFECTIVENESS) < 1.0E-4F, "Deveria ignorar 40% da armadura (efetividade " + pierced + ")");
		float plain = EnchantmentHelper.modifyArmorEffectiveness(level, new ItemStack(Items.DIAMOND_SWORD), zombie, source, 1.0F);
		helper.assertTrue(plain == 1.0F, "Espadas comuns não devem perfurar armadura");
		enderman.discard();
		zombie.discard();
		helper.succeed();
	}

	@GameTest(maxTicks = 200, skyAccess = true)
	public void echoBowArrowsFlyStraightAndFade(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper, new BlockPos(2, 1, 2));
		player.setXRot(-90.0F);
		ItemStack bow = new ItemStack(HHItems.ECHO_STRING_BOW);
		player.setItemInHand(InteractionHand.MAIN_HAND, bow);
		HHItems.ECHO_STRING_BOW.releaseUsing(bow, level, player, 72000 - 30);

		List<AbstractArrow> arrows = level.getEntitiesOfClass(AbstractArrow.class, player.getBoundingBox().inflate(4),
			arrow -> arrow.entityTags().contains(EchoStringBowItem.ECHO_TAG));
		helper.assertTrue(arrows.size() == 1, "O Arco de Eco deveria disparar uma flecha de eco (achou " + arrows.size() + ")");
		AbstractArrow arrow = arrows.getFirst();
		helper.assertTrue(arrow.isNoGravity(), "Flecha de eco não sofre gravidade");
		helper.assertTrue(arrow.getPierceLevel() >= EchoStringBowItem.PIERCE, "Flecha de eco atravessa 2 inimigos");
		helper.getLevel().getServer().getPlayerList().remove(player);
		helper.runAfterDelay(EchoStringBowItem.LIFETIME_TICKS + 10, () -> {
			helper.assertTrue(arrow.isRemoved(), "A flecha de eco deveria se dissipar no ar (tick " + arrow.tickCount + ", pos " + arrow.position()
				+ ", gravidade " + !arrow.isNoGravity() + ")");
			helper.succeed();
		});
	}

	@GameTest
	public void silkGliderRefusesRocketsAndGlidesGently(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper, new BlockPos(2, 3, 2));
		helper.assertTrue(new ItemStack(HHItems.SILK_GLIDER).has(DataComponents.GLIDER), "Asas de Seda devem planar (componente glider)");
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(HHItems.SILK_GLIDER));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIREWORK_ROCKET));
		player.startFallFlying();
		helper.assertTrue(player.isFallFlying(), "O jogador deveria estar planando");
		InteractionResult silk = Items.FIREWORK_ROCKET.use(level, player, InteractionHand.MAIN_HAND);
		helper.assertTrue(silk instanceof InteractionResult.Fail, "Foguete com Asas de Seda deveria falhar, veio " + silk);

		player.setDeltaMovement(3.0, -2.0, 0.0);
		Updrafts.tick(player);
		Vec3 motion = player.getDeltaMovement();
		helper.assertTrue(motion.length() <= Updrafts.SILK_GLIDER_MAX_SPEED + 1.0E-6, "Velocidade deveria ser limitada: " + motion.length());
		helper.assertTrue(motion.y >= Updrafts.SILK_GLIDER_MAX_SINK - 1.0E-6, "Queda deveria ser suave: " + motion.y);

		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
		player.startFallFlying();
		InteractionResult elytra = Items.FIREWORK_ROCKET.use(level, player, InteractionHand.MAIN_HAND);
		helper.assertTrue(!(elytra instanceof InteractionResult.Fail), "Com Elytra o foguete continua funcionando");
		done(helper, player);
	}

	@GameTest
	public void nullStepBootsDoubleJump(GameTestHelper helper) {
		ServerPlayer player = player(helper, new BlockPos(2, 3, 2));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(HHItems.NULL_STEP_BOOTS));
		NullStepHandler.tick(player);
		helper.assertTrue(player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) > 3.0, "Botas aumentam a queda segura");

		player.setOnGround(false);
		player.fallDistance = 12.0;
		helper.assertTrue(NullStepHandler.canDoubleJump(player), "Deveria poder dar o segundo pulo no ar");
		NullStepHandler.onDoubleJump(player);
		helper.assertTrue(player.fallDistance == 0.0, "O salto duplo zera a distância de queda");
		player.fallDistance = 12.0;
		NullStepHandler.onDoubleJump(player);
		helper.assertTrue(player.fallDistance == 12.0, "Só um salto extra por pulo");
		done(helper, player);
	}

	@GameTest(dimension = "minecraft:the_end")
	public void nullStepBootsSoftenEndFalls(GameTestHelper helper) {
		ServerPlayer player = player(helper, new BlockPos(2, 3, 2));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(HHItems.NULL_STEP_BOOTS));
		NullStepHandler.tick(player);
		double multiplier = player.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER);
		helper.assertTrue(multiplier <= 0.21, "No End o dano de queda deveria cair 80% (multiplicador " + multiplier + ")");
		done(helper, player);
	}

	@GameTest
	public void updraftVentLiftsPlayers(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 1, 2), HHBlocks.UPDRAFT_VENT);
		ServerPlayer player = player(helper, new BlockPos(2, 5, 2));
		player.setDeltaMovement(0.0, -0.4, 0.0);
		player.fallDistance = 6.0;
		Updrafts.tick(player);
		helper.assertTrue(player.getDeltaMovement().y > -0.4, "O gêiser deveria empurrar para cima");
		helper.assertTrue(player.fallDistance == 0.0, "O gêiser zera a queda");
		done(helper, player);
	}

	@GameTest
	public void resonanceCompassTunesTargets(GameTestHelper helper) {
		ServerPlayer player = player(helper, new BlockPos(1, 1, 1));
		ItemStack compass = new ItemStack(HHItems.RESONANCE_COMPASS);
		player.setItemInHand(InteractionHand.MAIN_HAND, compass);
		player.setShiftKeyDown(true);
		HHItems.RESONANCE_COMPASS.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(player.getMainHandItem().getOrDefault(HHComponents.RESONANCE_TARGET, 0) == 1, "Agachar + usar troca o alvo");
		player.setShiftKeyDown(false);
		InteractionResult outside = HHItems.RESONANCE_COMPASS.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(outside instanceof InteractionResult.Fail, "Fora do End os cristais não ressoam");
		done(helper, player);
	}

}
