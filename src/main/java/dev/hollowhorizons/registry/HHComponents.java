package dev.hollowhorizons.registry;

import com.mojang.serialization.Codec;
import dev.hollowhorizons.HollowHorizons;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

/** Componentes de dados próprios dos itens novos. */
public final class HHComponents {
	/** Cargas da Âncora do Vazio (0..{@value dev.hollowhorizons.item.VoidAnchorItem#MAX_CHARGES}). */
	public static final DataComponentType<Integer> ANCHOR_CHARGES = register("anchor_charges",
		DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 64)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	/** Alvo atual da Bússola de Ressonância (índice em {@link dev.hollowhorizons.item.ResonanceCompassItem.Target}). */
	public static final DataComponentType<Integer> RESONANCE_TARGET = register("resonance_target",
		DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 16)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	private HHComponents() {}

	private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, HollowHorizons.id(name), type);
	}

	public static void init() {}
}
