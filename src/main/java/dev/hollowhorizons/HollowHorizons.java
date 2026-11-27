package dev.hollowhorizons;

import dev.hollowhorizons.gameplay.NullStepHandler;
import dev.hollowhorizons.gameplay.PlayerTickHandler;
import dev.hollowhorizons.network.HHNetworking;
import dev.hollowhorizons.registry.HHAttachments;
import dev.hollowhorizons.registry.HHBlocks;
import dev.hollowhorizons.registry.HHComponents;
import dev.hollowhorizons.registry.HHCreativeTab;
import dev.hollowhorizons.registry.HHItems;
import dev.hollowhorizons.registry.HHParticles;
import dev.hollowhorizons.registry.HHWorldgen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hollow Horizons — a expansão do The End.
 *
 * <p>Ponto de entrada comum (servidor e cliente). A ordem importa: componentes e
 * partículas antes dos blocos, blocos antes dos itens, itens antes da aba criativa,
 * e o worldgen por último (as árvores referenciam blocos e as features, itens).
 */
public final class HollowHorizons implements ModInitializer {
	public static final String MOD_ID = "hollowhorizons";
	public static final Logger LOGGER = LoggerFactory.getLogger("Hollow Horizons");

	@Override
	public void onInitialize() {
		HHComponents.init();
		HHAttachments.init();
		HHParticles.init();
		HHBlocks.init();
		HHItems.init();
		HHCreativeTab.init();
		HHWorldgen.init();
		HHNetworking.init();
		PlayerTickHandler.init();
		NullStepHandler.init();
		LOGGER.info("Hollow Horizons carregado: o End nunca mais será vazio.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
