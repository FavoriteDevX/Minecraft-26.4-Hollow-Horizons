package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import dev.hollowhorizons.item.EchoStringBowItem;
import dev.hollowhorizons.item.HollowCorePickaxeItem;
import dev.hollowhorizons.item.ResonanceCompassItem;
import dev.hollowhorizons.item.VitreousCleaverItem;
import dev.hollowhorizons.item.VoidAnchorItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;

/** Itens e equipamentos do The End. */
public final class HHItems {
	/** Ordem de exibição na aba criativa. */
	public static final List<Item> CREATIVE_ORDER = new ArrayList<>();

	// ---------------------------------------------------------------- materiais
	/** Lâmina vítrea: entre diamante e netherite, mas pesada. */
	public static final ToolMaterial VITREOUS = new ToolMaterial(
		BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.5F, 12, HHTags.Items.REPAIRS_VITREOUS);
	/** Núcleo oco: minera como netherite e devora o End. */
	public static final ToolMaterial HOLLOW_CORE = new ToolMaterial(
		BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1800, 9.0F, 4.0F, 15, HHTags.Items.REPAIRS_HOLLOW_CORE);

	public static final ResourceKey<EquipmentAsset> NULL_STEP_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, HollowHorizons.id("null_step"));
	public static final ResourceKey<EquipmentAsset> SILK_GLIDER_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, HollowHorizons.id("silk_glider"));

	public static final ArmorMaterial NULL_STEP = new ArmorMaterial(
		33, Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3, ArmorType.BODY, 11),
		15, SoundEvents.ARMOR_EQUIP_ELYTRA, 2.0F, 0.0F, HHTags.Items.REPAIRS_NULL_STEP, NULL_STEP_ASSET);

	// ---------------------------------------------------------------- ingredientes
	public static final Item PALE_SILK_THREAD = register("pale_silk_thread", Item::new, new Item.Properties());
	public static final Item RESONANT_SHARD = register("resonant_shard", Item::new, new Item.Properties());
	public static final Item VITREOUS_SHARD = register("vitreous_shard", Item::new, new Item.Properties());
	public static final Item ECHO_STRING = register("echo_string", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item HOLLOW_CORE_ITEM = register("hollow_core", Item::new,
		new Item.Properties().rarity(Rarity.RARE).fireResistant());

	// ---------------------------------------------------------------- exploração e mobilidade
	public static final Item VOID_ANCHOR = register("void_anchor", VoidAnchorItem::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()
			.component(HHComponents.ANCHOR_CHARGES, 1));
	public static final Item SILK_GLIDER = register("silk_glider", Item::new,
		new Item.Properties().durability(180).rarity(Rarity.UNCOMMON)
			.component(DataComponents.GLIDER, Unit.INSTANCE)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
				.setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(SILK_GLIDER_ASSET).setDamageOnHurt(false).build())
			.repairable(PALE_SILK_THREAD));
	public static final Item NULL_STEP_BOOTS = register("null_step_boots", Item::new,
		new Item.Properties().humanoidArmor(NULL_STEP, ArmorType.BOOTS).rarity(Rarity.RARE));
	public static final Item RESONANCE_COMPASS = register("resonance_compass", ResonanceCompassItem::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(HHComponents.RESONANCE_TARGET, 0));

	// ---------------------------------------------------------------- combate e ferramentas
	public static final Item VITREOUS_CLEAVER = register("vitreous_cleaver", VitreousCleaverItem::new,
		new Item.Properties().sword(VITREOUS, 5.0F, -3.0F).rarity(Rarity.RARE));
	public static final Item ECHO_STRING_BOW = register("echo_string_bow", EchoStringBowItem::new,
		new Item.Properties().durability(500).enchantable(1).rarity(Rarity.RARE).repairable(ECHO_STRING));
	public static final Item HOLLOW_CORE_PICKAXE = register("hollow_core_pickaxe", HollowCorePickaxeItem::new,
		hollowCorePickaxeProperties());

	// ---------------------------------------------------------------- blocos
	public static final Item PALE_SILK_LOG = block(HHBlocks.PALE_SILK_LOG);
	public static final Item PALE_SILK_PLANKS = block(HHBlocks.PALE_SILK_PLANKS);
	public static final Item PALE_SILK_LEAVES = block(HHBlocks.PALE_SILK_LEAVES);
	public static final Item PALE_SILK_SAPLING = block(HHBlocks.PALE_SILK_SAPLING);
	public static final Item LUMINOUS_SILK = block(HHBlocks.LUMINOUS_SILK);
	public static final Item PALE_SILK_MOSS = block(HHBlocks.PALE_SILK_MOSS);
	public static final Item VITREOUS_OBSIDIAN = block(HHBlocks.VITREOUS_OBSIDIAN);
	public static final Item VITREOUS_SHARD_CLUSTER = block(HHBlocks.VITREOUS_SHARD_CLUSTER);
	public static final Item UPDRAFT_VENT = block(HHBlocks.UPDRAFT_VENT);
	public static final Item RESONANT_SPIRE = block(HHBlocks.RESONANT_SPIRE);
	public static final Item RESONANT_CLUSTER = block(HHBlocks.RESONANT_CLUSTER);
	public static final Item VOID_MUD = block(HHBlocks.VOID_MUD);
	public static final Item GLOWFEN_LILY = block(HHBlocks.GLOWFEN_LILY, PlaceOnWaterBlockItem::new);
	public static final Item VOIDREED = block(HHBlocks.VOIDREED);
	public static final Item FEN_BRICKS = block(HHBlocks.FEN_BRICKS);
	public static final Item CRACKED_FEN_BRICKS = block(HHBlocks.CRACKED_FEN_BRICKS);

	private HHItems() {}

	/**
	 * A picareta usa as regras normais de picareta de netherite, mas com uma regra extra
	 * na frente: blocos do End (tag {@code hollowhorizons:hollow_core_instant}) quebram com
	 * velocidade 1000 — rápido o bastante para cair em um único tick (instantâneo).
	 */
	private static Item.Properties hollowCorePickaxeProperties() {
		HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return new Item.Properties().pickaxe(HOLLOW_CORE, 1.0F, -2.8F).rarity(Rarity.EPIC).fireResistant()
			.component(DataComponents.TOOL, new Tool(List.of(
				Tool.Rule.minesAndDrops(blocks.getOrThrow(HHTags.Blocks.HOLLOW_CORE_INSTANT), 1000.0F),
				Tool.Rule.deniesDrops(blocks.getOrThrow(HOLLOW_CORE.incorrectBlocksForDrops())),
				Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE), HOLLOW_CORE.speed())
			), 1.0F, 1, true));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HollowHorizons.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
		CREATIVE_ORDER.add(item);
		return item;
	}

	private static Item block(Block block) {
		return block(block, BlockItem::new);
	}

	private static Item block(Block block, BiFunction<Block, Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(block));
		Item item = factory.apply(block, new Item.Properties().useBlockDescriptionPrefix().setId(key));
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}
		Registry.register(BuiltInRegistries.ITEM, key, item);
		CREATIVE_ORDER.add(item);
		return item;
	}

	public static ItemStack charged(Item anchor, int charges) {
		ItemStack stack = new ItemStack(anchor);
		stack.set(HHComponents.ANCHOR_CHARGES, charges);
		return stack;
	}

	public static void init() {}
}
