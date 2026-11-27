package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class HHParticles {
	/** Esporos luminosos que flutuam nas Florestas de Seda Pálida. */
	public static final SimpleParticleType SILK_SPORE = Registry.register(
		BuiltInRegistries.PARTICLE_TYPE, HollowHorizons.id("silk_spore"), FabricParticleTypes.simple(false));

	private HHParticles() {}

	public static void init() {}
}
