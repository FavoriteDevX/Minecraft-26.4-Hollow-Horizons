package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import dev.hollowhorizons.world.feature.ChasmRiftFeature;
import dev.hollowhorizons.world.feature.FenRuinFeature;
import dev.hollowhorizons.world.feature.PlasmaPoolFeature;
import dev.hollowhorizons.world.feature.ResonantSpireFeature;
import dev.hollowhorizons.world.feature.VitreousSpireFeature;
import dev.hollowhorizons.world.structure.VoidRuinsPiece;
import dev.hollowhorizons.world.structure.VoidRuinsStructure;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Worldgen da expansão: tipos de feature, a estrutura Ruínas do Vazio e a injeção dos
 * quatro biomas no gerador do End. Os biomas, features configuradas e regras de superfície
 * em si são dados (JSON em {@code data/hollowhorizons/worldgen}).
 */
public final class HHWorldgen {
	// ---------------------------------------------------------------- biomas
	public static final ResourceKey<Biome> PALE_SILK_WOODS = biome("pale_silk_woods");
	public static final ResourceKey<Biome> VITREOUS_CHASMS = biome("vitreous_chasms");
	public static final ResourceKey<Biome> RESONANT_SPIREFIELDS = biome("resonant_spirefields");
	public static final ResourceKey<Biome> VOID_FEN = biome("void_fen");
	public static final List<ResourceKey<Biome>> ALL_BIOMES = List.of(PALE_SILK_WOODS, VITREOUS_CHASMS, RESONANT_SPIREFIELDS, VOID_FEN);

	/**
	 * Pesos entre as "terras altas" do End (o vanilla end_highlands entra primeiro, com 1.0).
	 *
	 * <p>O Fabric sorteia com {@code |ruído Perlin| × soma dos pesos}, e o valor absoluto do
	 * Perlin se concentra perto de zero (mediana ≈ 0,19). Pesos iguais fariam o último bioma
	 * da lista quase nunca aparecer. Estes valores foram calculados a partir dos quantis
	 * medidos do ruído (ver o GameTest {@code theFourBiomesGenerateInTheOuterEnd}) para dar
	 * ≈36% às terras altas vanilla e ≈16% a cada bioma novo.
	 */
	public static final Map<ResourceKey<Biome>, Double> HIGHLANDS_WEIGHTS = new LinkedHashMap<>();

	static {
		HIGHLANDS_WEIGHTS.put(PALE_SILK_WOODS, 0.51);
		HIGHLANDS_WEIGHTS.put(VITREOUS_CHASMS, 0.58);
		HIGHLANDS_WEIGHTS.put(RESONANT_SPIREFIELDS, 0.82);
		HIGHLANDS_WEIGHTS.put(VOID_FEN, 4.76);
	}

	// ---------------------------------------------------------------- features (chaves usadas por código)
	public static final ResourceKey<Feature> PALE_SILK_TREE = ResourceKey.create(Registries.FEATURE, HollowHorizons.id("pale_silk_tree"));

	// ---------------------------------------------------------------- tipos de feature
	static {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, HollowHorizons.id("vitreous_spire"), VitreousSpireFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, HollowHorizons.id("chasm_rift"), ChasmRiftFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, HollowHorizons.id("plasma_pool"), PlasmaPoolFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, HollowHorizons.id("resonant_spire"), ResonantSpireFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, HollowHorizons.id("fen_ruin"), FenRuinFeature.CODEC);
	}

	// ---------------------------------------------------------------- estrutura
	public static final StructurePieceType VOID_RUINS_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
		HollowHorizons.id("void_ruins"), (StructurePieceType.ContextlessType) VoidRuinsPiece::new);
	public static final StructureType<VoidRuinsStructure> VOID_RUINS_TYPE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
		HollowHorizons.id("void_ruins"), () -> VoidRuinsStructure.CODEC);

	private HHWorldgen() {}

	private static ResourceKey<Biome> biome(String name) {
		return ResourceKey.create(Registries.BIOME, HollowHorizons.id(name));
	}

	public static void init() {
		for (ResourceKey<Biome> biome : ALL_BIOMES) {
			TheEndBiomes.addHighlandsBiome(biome, HIGHLANDS_WEIGHTS.get(biome));
			// O mesmo bioma cobre também as "terras médias" da ilha: a paisagem fica contínua.
			TheEndBiomes.addMidlandsBiome(biome, biome, 1.0);
		}
	}
}
