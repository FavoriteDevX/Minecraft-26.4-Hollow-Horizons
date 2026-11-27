package dev.hollowhorizons.world.structure;

import com.mojang.serialization.MapCodec;
import dev.hollowhorizons.registry.HHWorldgen;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Ruínas do Vazio: um templo afundado de uma civilização anterior aos endermen, com um
 * baú no altar. É o alvo "Ruínas do Vazio" da Bússola de Ressonância.
 *
 * <p>Só nasce onde a ilha é larga e alta o bastante (todos os cantos acima de Y 40), para
 * nunca ficar pendurado sobre o Vazio.
 */
public class VoidRuinsStructure extends Structure {
	public static final MapCodec<VoidRuinsStructure> CODEC = simpleCodec(VoidRuinsStructure::new);
	public static final int MIN_SURFACE_Y = 40;

	public VoidRuinsStructure(Structure.StructureSettings settings) {
		super(settings);
	}

	@Override
	protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
		ChunkPos chunk = context.chunkPos();
		int x = chunk.getMinBlockX();
		int z = chunk.getMinBlockZ();
		int lowest = getLowestY(context, x, z, VoidRuinsPiece.WIDTH, VoidRuinsPiece.DEPTH);
		if (lowest < MIN_SURFACE_Y) {
			return Optional.empty();
		}
		int centerY = context.chunkGenerator().getFirstOccupiedHeight(x + VoidRuinsPiece.WIDTH / 2, z + VoidRuinsPiece.DEPTH / 2,
			Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
		BlockPos start = new BlockPos(x, Math.min(lowest, centerY) - 2, z);
		return Optional.of(new Structure.GenerationStub(start, builder -> builder.addPiece(new VoidRuinsPiece(context.random(), start))));
	}

	@Override
	public StructureType<?> type() {
		return HHWorldgen.VOID_RUINS_TYPE;
	}
}
