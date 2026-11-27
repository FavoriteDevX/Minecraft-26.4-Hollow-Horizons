package dev.hollowhorizons.test;

import dev.hollowhorizons.HollowHorizons;
import dev.hollowhorizons.registry.HHCreativeTab;
import dev.hollowhorizons.registry.HHItems;
import dev.hollowhorizons.registry.HHTags;
import dev.hollowhorizons.registry.HHWorldgen;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Teste com o CLIENTE de verdade (janela, renderização, texturas, entrada de teclado e mouse).
 *
 * <p>Abre o jogo, cria um mundo, usa cada item da atualização como um jogador usaria e visita
 * os quatro biomas novos e as Ruínas do Vazio, salvando screenshots tiradas pelo próprio jogo em
 * {@code build/hollow-horizons-screenshots/}. Rode com {@code ./gradlew runClientGameTest}
 * (em servidores sem tela: {@code xvfb-run ./gradlew runClientGameTest}).
 */
public class HollowHorizonsClientGameTest implements FabricClientGameTest {
	/** No 26.3 (SDL3) os botões seguem a numeração do SDL: esquerdo = 1, direito = 3. */
	private static final int MOUSE_LEFT = InputConstants.MOUSE_BUTTON_LEFT;
	private static final int MOUSE_RIGHT = InputConstants.MOUSE_BUTTON_RIGHT;
	private static final int CHUNK_TIMEOUT = 12_000;
	private static final String SCREENSHOT_LANGUAGE = System.getProperty("hollowhorizons.screenshotLanguage", "en_us");

	private final List<String> failures = new ArrayList<>();
	private ClientGameTestContext context;
	private TestSingleplayerContext world;
	private TestServerContext server;

	@Override
	public void runTest(ClientGameTestContext context) {
		this.context = context;
		context.getInput().resizeWindow(1280, 720);
		context.waitTicks(20);
		context.runOnClient(minecraft -> {
			minecraft.options.renderDistance().set(6);
			minecraft.options.gamma().set(1.0);
			// Screenshots em inglês (as do README). Para ver em português: troque por "pt_br".
			minecraft.options.languageCode = SCREENSHOT_LANGUAGE;
			minecraft.getLanguageManager().setSelected(SCREENSHOT_LANGUAGE);
			minecraft.reloadResourcePacks();
		});
		context.waitTicks(5);
		context.waitFor(minecraft -> minecraft.gui.overlay() == null, 6000);
		context.waitTicks(20);
		scene("00_title_screen", () -> save("00_title_screen"));

		try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(s -> {
			s.setSeed("26042026");
			s.setGenerateStructures(true); // o construtor de testes do Fabric desliga estruturas por padrão
		}).create()) {
			this.world = world;
			this.server = world.getServer();
			world.getConnection().waitForChunksRender(CHUNK_TIMEOUT);
			run("difficulty peaceful");
			run("gamerule fall_damage false");
			run("gamerule show_advancement_messages false");
			context.runOnClient(minecraft -> minecraft.options.chatVisibility().set(ChatVisiblity.HIDDEN));

			diagnoseStructures();

			// Palco: o centro de uma Floresta de Seda Pálida, nas ilhas externas.
			BlockPos stage = biomeCenter(HHWorldgen.PALE_SILK_WOODS);
			teleport(stage, -45, 10);

			scene("01_inventory", this::inventory);
			scene("02_creative_tab", this::creativeTab);
			scene("03_equipment", () -> equipment(stage));
			scene("04_void_anchor", () -> voidAnchor(stage));
			scene("05_resonance_compass", () -> resonanceCompass(stage));
			scene("06_echo_string_bow", () -> echoBow(stage));
			scene("07_vitreous_cleaver", () -> cleaver(stage));
			scene("08_hollow_core_pickaxe", () -> pickaxe(stage));
			scene("09_silk_glider", () -> glider(stage));
			scene("10_updraft_vent", () -> updraft(stage));

			int index = 11;
			hud(false);
			run("gamemode spectator @a");
			for (ResourceKey<Biome> biome : HHWorldgen.ALL_BIOMES) {
				String name = String.format("%02d_%s", index++, biome.identifier().getPath());
				scene(name, () -> {
					lookFromAbove(biomeCenter(biome), 16, 20);
					save(name);
				});
			}
			String ruinsName = String.format("%02d_void_ruins", index);
			scene(ruinsName, () -> {
				lookFromAbove(ruins(), 12, 10);
				save(ruinsName);
			});
			hud(true);
		}

		if (!this.failures.isEmpty()) {
			throw new AssertionError("Cenas que falharam: " + this.failures);
		}
	}

	// ------------------------------------------------------------------ cenas

	private void inventory() {
		run("gamemode survival @a");
		run("clear @a");
		for (Item item : HHItems.CREATIVE_ORDER) {
			String id = BuiltInRegistries.ITEM.getKey(item).toString();
			run(item == HHItems.VOID_ANCHOR ? "give @a " + id + "[hollowhorizons:anchor_charges=4]" : "give @a " + id);
		}
		this.context.waitTicks(10);
		this.context.getInput().pressKey(options -> options.keyInventory);
		this.context.waitTicks(10);
		this.context.getInput().setCursorPos(8, 8);
		this.context.waitTicks(2);
		save("01_inventory");
		closeScreen();
	}

	private void creativeTab() {
		run("gamemode creative @a");
		this.context.waitTicks(10);
		this.context.runOnClient(minecraft -> minecraft.gui.setScreen(
			new CreativeModeInventoryScreen(minecraft.player, minecraft.player.connection.enabledFeatures(), false)));
		this.context.waitTicks(5);
		this.context.runOnClient(minecraft -> {
			if (minecraft.gui.screen() instanceof FabricCreativeModeInventoryScreen screen) {
				screen.setSelectedTab(HHCreativeTab.HOLLOW_HORIZONS);
			}
		});
		this.context.getInput().setCursorPos(8, 8);
		this.context.waitTicks(10);
		save("02_creative_tab");
		closeScreen();
	}

	private void equipment(BlockPos stage) {
		run("clear @a");
		run("item replace entity @a armor.chest with hollowhorizons:silk_glider");
		run("item replace entity @a armor.feet with hollowhorizons:null_step_boots");
		run("item replace entity @a weapon.mainhand with hollowhorizons:vitreous_cleaver");
		run("item replace entity @a weapon.offhand with hollowhorizons:void_anchor");
		run("gamemode creative @a");
		run("execute in minecraft:the_end run tp @a " + stage.getX() + ".5 " + (stage.getY() + 4) + " " + stage.getZ() + ".5 -45 20");
		this.world.getConnection().waitForChunksRender(CHUNK_TIMEOUT);
		this.context.runOnClient(minecraft -> {
			minecraft.player.getAbilities().flying = true;
			minecraft.player.onUpdateAbilities();
		});
		hud(false);
		camera(CameraType.THIRD_PERSON_BACK);
		this.context.waitTicks(40);
		save("03_equipment");
		camera(CameraType.FIRST_PERSON);
		hud(true);
	}

	private void voidAnchor(BlockPos stage) {
		run("gamemode survival @a");
		run("clear @a");
		run("item replace entity @a hotbar.0 with hollowhorizons:void_anchor[hollowhorizons:anchor_charges=4]");
		run("give @a minecraft:ender_pearl 8");
		hotbar(0);
		teleport(stage, -45, 20);
		this.context.waitTicks(20); // o servidor memoriza este chão como "último bloco sólido"
		// Cai no Vazio: abaixo do fundo do mundo, onde a âncora puxa de volta.
		run("execute in minecraft:the_end run tp @a " + stage.getX() + " -8 " + stage.getZ());
		this.context.waitTicks(6);
		save("04_void_anchor");
		double y = this.context.computeOnClient(minecraft -> minecraft.player.getY());
		if (y < stage.getY() - 1) {
			throw new IllegalStateException("A âncora não puxou o jogador de volta (y=" + y + ")");
		}
	}

	private void resonanceCompass(BlockPos stage) {
		run("clear @a");
		run("item replace entity @a hotbar.0 with hollowhorizons:resonance_compass[hollowhorizons:resonance_target=1]");
		run("give @a hollowhorizons:resonant_shard 8");
		hotbar(0);
		teleport(stage, -45, 10);
		this.context.waitTicks(10);
		this.context.waitTicks(60); // deixa a mensagem da âncora sumir
		this.context.getInput().pressMouse(MOUSE_RIGHT);
		this.context.waitFor(minecraft -> minecraft.player.getMainHandItem().has(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER), 2400);
		this.context.waitTicks(10);
		save("05_resonance_compass");
	}

	private void echoBow(BlockPos stage) {
		run("clear @a");
		run("item replace entity @a hotbar.0 with hollowhorizons:echo_string_bow");
		run("give @a minecraft:arrow 16");
		hotbar(0);
		teleport(stage, -45, -8);
		this.context.waitTicks(10);
		this.context.getInput().holdMouse(MOUSE_RIGHT);
		this.context.waitTicks(22); // arco totalmente puxado (texturas de "pulling")
		save("06_echo_string_bow");
		this.context.getInput().releaseMouse(MOUSE_RIGHT);
		this.context.waitTicks(4);
		long echoArrows = this.server.computeOnServer(s -> s.getLevel(Level.END)
			.getEntitiesOfClass(net.minecraft.world.entity.projectile.arrow.AbstractArrow.class,
				net.minecraft.world.phys.AABB.ofSize(net.minecraft.world.phys.Vec3.atCenterOf(stage), 128, 128, 128),
				arrow -> arrow.entityTags().contains(dev.hollowhorizons.item.EchoStringBowItem.ECHO_TAG) && arrow.isNoGravity())
			.size());
		if (echoArrows == 0) {
			throw new IllegalStateException("O Arco de Eco não disparou uma flecha sem gravidade");
		}
	}

	private void cleaver(BlockPos stage) {
		run("clear @a");
		run("difficulty easy");
		run("item replace entity @a hotbar.0 with hollowhorizons:vitreous_cleaver");
		hotbar(0);
		teleport(stage, 0, 10);
		run("execute in minecraft:the_end run summon minecraft:enderman " + stage.getX() + ".5 " + stage.getY() + " " + (stage.getZ() + 3) + ".5 {NoAI:1b,Rotation:[180f,0f]}");
		this.context.waitTicks(10);
		this.context.getInput().lookAt(stage.offset(0, 2, 3));
		this.context.waitTicks(2);
		this.context.getInput().pressMouse(MOUSE_LEFT);
		this.context.waitTicks(3);
		save("07_vitreous_cleaver");
		run("kill @e[type=minecraft:enderman]");
		run("difficulty peaceful");
	}

	private void pickaxe(BlockPos stage) {
		run("gamemode survival @a");
		run("clear @a");
		run("item replace entity @a hotbar.0 with hollowhorizons:hollow_core_pickaxe");
		hotbar(0);
		BlockPos block = stage.offset(0, 0, 2);
		run("execute in minecraft:the_end run setblock " + block.getX() + " " + block.getY() + " " + block.getZ() + " minecraft:end_stone");
		teleport(stage, 0, 35);
		this.context.waitTicks(10);
		this.context.getInput().lookAt(block);
		this.context.waitTicks(2);
		this.context.getInput().holdMouseFor(MOUSE_LEFT, 10);
		this.context.waitTicks(4);
		this.context.getInput().pressKey(options -> options.keyInventory);
		this.context.waitTicks(8);
		save("08_hollow_core_pickaxe");
		closeScreen();
		boolean pickedUp = this.server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getInventory()
			.contains(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.END_STONE)));
		if (!pickedUp) {
			throw new IllegalStateException("A pedra do End minerada não foi para o inventário");
		}
	}

	private void glider(BlockPos stage) {
		run("gamemode survival @a");
		run("clear @a");
		run("item replace entity @a armor.chest with hollowhorizons:silk_glider");
		run("execute in minecraft:the_end run tp @a " + stage.getX() + " " + (stage.getY() + 45) + " " + stage.getZ() + " -45 10");
		world.getConnection().waitForChunksRender(CHUNK_TIMEOUT);
		this.context.waitTicks(6);
		this.context.getInput().pressKey(options -> options.keyJump);
		this.context.waitTicks(3);
		this.context.runOnClient(minecraft -> {
			if (!minecraft.player.isFallFlying() && minecraft.player.tryToStartFallFlying()) {
				minecraft.player.connection.send(new ServerboundPlayerCommandPacket(minecraft.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
			}
		});
		this.context.waitTicks(25);
		boolean gliding = this.context.computeOnClient(minecraft -> minecraft.player.isFallFlying());
		if (!gliding) {
			throw new IllegalStateException("As Asas de Seda não abriram");
		}
		hud(false);
		camera(CameraType.THIRD_PERSON_BACK);
		this.context.waitTicks(5);
		save("09_silk_glider");
		camera(CameraType.FIRST_PERSON);
		hud(true);
		run("gamemode creative @a");
	}

	private void updraft(BlockPos stage) {
		BlockPos vent = stage.offset(3, -1, 3);
		run("execute in minecraft:the_end run setblock " + vent.getX() + " " + vent.getY() + " " + vent.getZ() + " hollowhorizons:updraft_vent");
		run("gamemode survival @a");
		run("clear @a");
		run("execute in minecraft:the_end run tp @a " + vent.getX() + ".5 " + (vent.getY() + 1) + " " + vent.getZ() + ".5 -45 55");
		this.context.waitTicks(25);
		hud(false);
		camera(CameraType.THIRD_PERSON_BACK);
		this.context.waitTicks(3);
		double lifted = this.context.computeOnClient(minecraft -> minecraft.player.getY()) - vent.getY();
		if (lifted < 3) {
			throw new IllegalStateException("O gêiser não ergueu o jogador (altura " + lifted + ")");
		}
		save("10_updraft_vent");
		camera(CameraType.FIRST_PERSON);
		hud(true);
		run("gamemode creative @a");
	}

	// ------------------------------------------------------------------ utilidades

	private interface Scene {
		void run() throws Exception;
	}

	private void scene(String name, Scene scene) {
		closeScreen();
		try {
			scene.run();
		} catch (Throwable t) {
			HollowHorizons.LOGGER.error("[client-gametest] cena {} falhou", name, t);
			this.failures.add(name + ": " + t.getMessage());
			this.context.runOnClient(minecraft -> {
				if (minecraft.gui.screen() != null) {
					minecraft.gui.setScreen(null);
				}
			});
		}
		boolean dead = this.context.computeOnClient(minecraft -> minecraft.player != null && minecraft.player.isDeadOrDying());
		if (dead) {
			this.failures.add(name + ": o jogador morreu");
			this.context.runOnClient(minecraft -> minecraft.player.respawn());
			this.context.waitTicks(20);
		}
	}

	/** Fecha qualquer tela (inventário, aba criativa) para que cliques e teclas voltem ao mundo. */
	private void closeScreen() {
		this.context.runOnClient(minecraft -> {
			if (minecraft.gui.screen() != null) {
				minecraft.gui.setScreen(null);
			}
		});
		this.context.waitTicks(3);
	}

	private void run(String command) {
		this.server.runCommand(command);
	}

	private void hotbar(int slot) {
		this.context.getInput().pressKey(options -> options.keyHotbarSlots[slot]);
		this.context.waitTicks(2);
	}

	private void hud(boolean visible) {
		this.context.runOnClient(minecraft -> {
			if (minecraft.gui.hud.isHidden() == visible) {
				minecraft.gui.hud.toggle();
			}
		});
	}

	private void camera(CameraType type) {
		this.context.runOnClient(minecraft -> minecraft.options.setCameraType(type));
	}

	private void teleport(BlockPos pos, float yaw, float pitch) {
		run("execute in minecraft:the_end run tp @a " + pos.getX() + ".5 " + pos.getY() + " " + pos.getZ() + ".5 " + yaw + " " + pitch);
		this.world.getConnection().waitForChunksRender(CHUNK_TIMEOUT);
	}

	private void lookFromAbove(BlockPos target, int distance, int height) {
		run("execute in minecraft:the_end run tp @a " + (target.getX() - distance) + " " + (target.getY() + height) + " " + (target.getZ() - distance)
			+ " facing " + target.getX() + " " + target.getY() + " " + target.getZ());
		this.world.getConnection().waitForChunksRender(CHUNK_TIMEOUT);
		this.context.waitTicks(60);
	}

	private BlockPos biomeCenter(ResourceKey<Biome> biome) {
		return this.server.computeOnServer(s -> {
			ServerLevel end = s.getLevel(Level.END);
			var found = end.findClosestBiome3d(h -> h.is(biome), new BlockPos(1600, 64, 0), 6400, 32, 64);
			BlockPos start = found == null ? new BlockPos(1600, 64, 0) : found.getFirst();
			BlockPos best = start;
			int bestScore = -1;
			for (int dx = -96; dx <= 96; dx += 32) {
				for (int dz = -96; dz <= 96; dz += 32) {
					BlockPos candidate = start.offset(dx, 0, dz);
					int score = 0;
					for (int ox = -24; ox <= 24; ox += 12) {
						for (int oz = -24; oz <= 24; oz += 12) {
							if (end.getBiome(candidate.offset(ox, 0, oz)).is(biome)) {
								score++;
							}
						}
					}
					end.getChunk(candidate.getX() >> 4, candidate.getZ() >> 4);
					int y = end.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ());
					int weighted = score * 1000 + y; // mais bioma primeiro, depois o topo mais alto da ilha
					if (y > 45 && weighted > bestScore) {
						bestScore = weighted;
						best = new BlockPos(candidate.getX(), y, candidate.getZ());
					}
				}
			}
			return best;
		});
	}

	/** Registra no log como o gerador do End enxerga os biomas e as estruturas desta atualização. */
	private void diagnoseStructures() {
		this.server.runOnServer(s -> {
			ServerLevel end = s.getLevel(Level.END);
			var biomeRegistry = end.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME);
			var possible = end.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
			for (ResourceKey<Biome> key : HHWorldgen.ALL_BIOMES) {
				var serverHolder = biomeRegistry.getOrThrow(key);
				boolean byIdentity = possible.contains(serverHolder);
				boolean byKey = possible.stream().anyMatch(h -> h.is(key));
				HollowHorizons.LOGGER.info("[client-gametest] diag bioma {}: possibleBiomes por identidade={} por chave={}", key.identifier(), byIdentity, byKey);
			}
			var structures = end.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
			for (String id : new String[]{"hollowhorizons:void_ruins", "minecraft:end_city"}) {
				var holder = structures.getOrThrow(ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE, net.minecraft.resources.Identifier.parse(id)));
				int placements = end.getChunkSource().getGeneratorState().getPlacementsForStructure(holder).size();
				HollowHorizons.LOGGER.info("[client-gametest] diag estrutura {}: placements={}", id, placements);
			}
		});
	}

	private BlockPos ruins() {
		return this.server.computeOnServer(s -> {
			ServerLevel end = s.getLevel(Level.END);
			long start = System.currentTimeMillis();
			BlockPos found = end.findNearestMapStructure(HHTags.Structures.RESONANCE_VOID_RUINS, new BlockPos(1600, 64, 0), 100, false);
			HollowHorizons.LOGGER.info("[client-gametest] busca das Ruínas do Vazio: {} em {} ms", found, System.currentTimeMillis() - start);
			if (found == null) {
				throw new IllegalStateException("Nenhuma Ruína do Vazio encontrada");
			}
			BlockPos center = found.offset(6, 0, 6);
			end.getChunk(center.getX() >> 4, center.getZ() >> 4);
			return new BlockPos(center.getX(), end.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ()), center.getZ());
		});
	}

	private void save(String name) {
		this.context.runOnClient(minecraft -> minecraft.gui.toastManager().clear());
		this.context.waitTicks(1);
		Path shot = this.context.takeScreenshot(name);
		try {
			Path out = Path.of("build/hollow-horizons-screenshots").toAbsolutePath();
			Files.createDirectories(out);
			Files.copy(shot, out.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
			HollowHorizons.LOGGER.info("[client-gametest] screenshot {} -> {}", name, out.resolve(name + ".png"));
		} catch (Exception e) {
			HollowHorizons.LOGGER.warn("[client-gametest] não consegui copiar {}: {}", shot, e.toString());
		}
	}
}
