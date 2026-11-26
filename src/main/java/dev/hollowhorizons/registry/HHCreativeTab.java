package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HHCreativeTab {
	public static final CreativeModeTab HOLLOW_HORIZONS = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, HollowHorizons.id("hollow_horizons"),
		FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.hollowhorizons"))
			.icon(() -> new ItemStack(HHItems.VOID_ANCHOR))
			.displayItems((parameters, output) -> {
				for (Item item : HHItems.CREATIVE_ORDER) {
					if (item == HHItems.VOID_ANCHOR) {
						output.accept(HHItems.charged(item, 4));
					} else {
						output.accept(item);
					}
				}
			})
			.build());

	private HHCreativeTab() {}

	public static void init() {}
}
