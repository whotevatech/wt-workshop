package net.wtworkshop;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import net.wtworkshop.builder.PartBuilderBlock;
import net.wtworkshop.builder.PartBuilderMenu;
import net.wtworkshop.material.MaterialRecipe;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.part.CastItem;
import net.wtworkshop.part.LegacyPartItem;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.part.PartType;
import net.wtworkshop.part.PatternItem;
import net.wtworkshop.smeltery.AlloyRecipe;
import net.wtworkshop.smeltery.MeltingRecipe;
import net.wtworkshop.smeltery.SmelteryBlock;
import net.wtworkshop.smeltery.SmelteryBlockEntity;
import net.wtworkshop.smeltery.SmelteryMenu;
import net.wtworkshop.station.StationBlock;
import net.wtworkshop.station.StationMenu;
import net.wtworkshop.tool.ToolData;
import net.wtworkshop.tool.ToolType;
import net.wtworkshop.tool.WorkshopTool;

public final class Registration {
   public static final String ID = "wt_workshop";
   public static final Items ITEMS = DeferredRegister.createItems("wt_workshop");
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("wt_workshop");
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "wt_workshop");
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "wt_workshop");
   public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "wt_workshop");
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "wt_workshop");
   public static final DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "wt_workshop");
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "wt_workshop");
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolData>> TOOL_DATA = COMPONENTS.registerComponentType(
      "tool", b -> b.persistent(ToolData.CODEC).networkSynchronized(ToolData.STREAM_CODEC).cacheEncoding()
   );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PART_MATERIAL = COMPONENTS.registerComponentType(
      "material", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
   );
   public static final DeferredBlock<StationBlock> TOOL_STATION = BLOCKS.register("tool_station", () -> new StationBlock(wood()));
   public static final DeferredBlock<PartBuilderBlock> PART_BUILDER = BLOCKS.register("part_builder", () -> new PartBuilderBlock(wood()));
   public static final DeferredBlock<SmelteryBlock> SMELTERY = BLOCKS.register(
      "smeltery",
      () -> new SmelteryBlock(
            Properties.of()
               .mapColor(MapColor.COLOR_RED)
               .strength(3.5F)
               .requiresCorrectToolForDrops()
               .sound(SoundType.STONE)
               .lightLevel(s -> s.getValue(SmelteryBlock.LIT) ? 13 : 0)
         )
   );
   public static final DeferredItem<BlockItem> TOOL_STATION_ITEM = ITEMS.registerSimpleBlockItem(TOOL_STATION);
   public static final DeferredItem<BlockItem> PART_BUILDER_ITEM = ITEMS.registerSimpleBlockItem(PART_BUILDER);
   public static final DeferredItem<BlockItem> SMELTERY_ITEM = ITEMS.registerSimpleBlockItem(SMELTERY);
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmelteryBlockEntity>> SMELTERY_BE = BLOCK_ENTITIES.register(
      "smeltery", () -> Builder.of(SmelteryBlockEntity::new, new Block[]{(Block)SMELTERY.get()}).build(null)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<StationMenu>> STATION_MENU = MENUS.register(
      "tool_station", () -> new MenuType(StationMenu::new, FeatureFlags.DEFAULT_FLAGS)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<PartBuilderMenu>> PART_BUILDER_MENU = MENUS.register(
      "part_builder", () -> new MenuType(PartBuilderMenu::new, FeatureFlags.DEFAULT_FLAGS)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<SmelteryMenu>> SMELTERY_MENU = MENUS.register(
      "smeltery", () -> new MenuType(SmelteryMenu::new, FeatureFlags.DEFAULT_FLAGS)
   );
   public static final DeferredHolder<RecipeType<?>, RecipeType<MaterialRecipe>> MATERIAL_RECIPE = RECIPE_TYPES.register(
      "material", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("wt_workshop", "material"))
   );
   public static final DeferredHolder<RecipeType<?>, RecipeType<MeltingRecipe>> MELTING_RECIPE = RECIPE_TYPES.register(
      "melting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("wt_workshop", "melting"))
   );
   public static final DeferredHolder<RecipeType<?>, RecipeType<AlloyRecipe>> ALLOY_RECIPE = RECIPE_TYPES.register(
      "alloying", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("wt_workshop", "alloying"))
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MaterialRecipe>> MATERIAL_SERIALIZER = RECIPE_SERIALIZERS.register(
      "material", MaterialRecipe.Serializer::new
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MeltingRecipe>> MELTING_SERIALIZER = RECIPE_SERIALIZERS.register(
      "melting", MeltingRecipe.Serializer::new
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlloyRecipe>> ALLOY_SERIALIZER = RECIPE_SERIALIZERS.register(
      "alloying", AlloyRecipe.Serializer::new
   );
   public static final DeferredItem<Item> BLANK_PATTERN = ITEMS.registerSimpleItem("blank_pattern");
   public static final DeferredItem<CastItem> INGOT_CAST = ITEMS.register("ingot_cast", () -> new CastItem(null));
   public static final DeferredItem<Item> ROSE_GOLD_INGOT = ITEMS.registerSimpleItem("rose_gold_ingot");
   public static final DeferredItem<Item> AMETHYST_BRONZE_INGOT = ITEMS.registerSimpleItem("amethyst_bronze_ingot");
   public static final Map<net.wtworkshop.tool.SlotExpansion,DeferredItem<net.wtworkshop.part.ExpansionItem>> EXPANSIONS=new EnumMap<>(net.wtworkshop.tool.SlotExpansion.class);
   public static final Map<PartType, DeferredItem<PatternItem>> PATTERNS = new EnumMap<>(PartType.class);
   public static final Map<PartType, DeferredItem<CastItem>> CASTS = new EnumMap<>(PartType.class);
   public static final Map<PartType, DeferredItem<PartItem>> PARTS = new EnumMap<>(PartType.class);
   public static final Map<ToolType, DeferredItem<WorkshopTool>> TOOLS = new EnumMap<>(ToolType.class);
   public static final Map<String, DeferredItem<LegacyPartItem>> LEGACY_PARTS = new LinkedHashMap<>();
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB;

   private static Properties wood() {
      return Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD);
   }

   private static void legacy(String id, PartType type, String material) {
      LEGACY_PARTS.put(id, ITEMS.register(id, () -> new LegacyPartItem(type, material)));
   }

   private Registration() {
   }

   public static void register(IEventBus bus) {
      COMPONENTS.register(bus);
      BLOCKS.register(bus);
      ITEMS.register(bus);
      BLOCK_ENTITIES.register(bus);
      MENUS.register(bus);
      RECIPE_TYPES.register(bus);
      RECIPE_SERIALIZERS.register(bus);
      TABS.register(bus);
   }

   public static ItemStack part(PartType type, ToolMaterial material) {
      return PartItem.create(type, material);
   }

   static {
      for (PartType type : PartType.values()) {
         PATTERNS.put(type, ITEMS.register(type.id() + "_pattern", () -> new PatternItem(type)));
         CASTS.put(type, ITEMS.register(type.id() + "_cast", () -> new CastItem(type)));
         PARTS.put(type, ITEMS.register(type.id(), () -> new PartItem(type)));
      }

      for(var expansion:net.wtworkshop.tool.SlotExpansion.values())EXPANSIONS.put(expansion,ITEMS.register(expansion.id()+"_expansion_brace",()->new net.wtworkshop.part.ExpansionItem(expansion)));
      for (ToolType type : ToolType.values()) {
         TOOLS.put(type, ITEMS.register(type.id(), () -> new WorkshopTool(type)));
      }

      for (String m : List.of("wood", "stone", "copper", "iron", "gold", "diamond", "netherite")) {
         legacy(m + "_pickaxe_head", PartType.PICKAXE_HEAD, m);
         legacy(m + "_axe_head", PartType.AXE_HEAD, m);
         legacy(m + "_shovel_head", PartType.SHOVEL_HEAD, m);
      }

      for (String m : List.of("wood", "bamboo", "bone", "iron")) {
         legacy(m + "_handle", PartType.HANDLE, m);
      }

      for (String m : List.of("string", "leather", "honeycomb")) {
         legacy(m + "_binding", PartType.BINDING, m);
      }

      TAB = TABS.register(
         "main",
         () -> CreativeModeTab.builder()
               .title(Component.translatable("itemGroup.wt_workshop"))
               .icon(() -> WorkshopTool.create(ToolType.PICKAXE, ToolData.of("diamond", "wood", "leather")))
               .displayItems(
                  (params, out) -> {
                     out.accept((ItemLike)TOOL_STATION_ITEM.get());
                     out.accept((ItemLike)PART_BUILDER_ITEM.get());
                     out.accept((ItemLike)SMELTERY_ITEM.get());
                     out.accept((ItemLike)BLANK_PATTERN.get());
                     EXPANSIONS.values().forEach(e->out.accept(e.get()));
                     PATTERNS.values().forEach(p -> out.accept((ItemLike)p.get()));
                     out.accept((ItemLike)INGOT_CAST.get());
                     CASTS.values().forEach(c -> out.accept((ItemLike)c.get()));
                     out.accept((ItemLike)ROSE_GOLD_INGOT.get());
                     out.accept((ItemLike)AMETHYST_BRONZE_INGOT.get());

                     for (ToolType typexx : ToolType.values()) {
                        for (String head : List.of("wood", "iron", "diamond", "netherite")) {
                           out.accept(
                              WorkshopTool.create(
                                 typexx, ToolData.of(head, head.equals("netherite") ? "netherite" : "wood", head.equals("wood") ? "string" : "leather")
                              )
                           );
                        }
                     }

                     for (PartType typex : PartType.values()) {
                        for (ToolMaterial m : Materials.all()) {
                           if (m.supports(typex.role)) {
                              out.accept(PartItem.create(typex, m));
                           }
                        }
                     }
                  }
               )
               .build()
      );
   }
}
