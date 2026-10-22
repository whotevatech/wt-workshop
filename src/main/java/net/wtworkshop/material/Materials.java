package net.wtworkshop.material;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Tiers;
import net.wtworkshop.WorkshopConfig;
import org.jetbrains.annotations.Nullable;

public final class Materials {
   private static final Map<String, ToolMaterial> REGISTRY = new LinkedHashMap<>();
   public static final ToolMaterial WOOD = register(
      ToolMaterial.builder("wood", 12158291)
         .head(60, 2.0F, 0.0F, Tiers.WOOD)
         .handle(1.0F, 1.0F, 1.0F)
         .trait(PartRole.HEAD, Trait.ECOLOGICAL, 1)
         .trait(PartRole.HANDLE, Trait.ECOLOGICAL, 1)
         .build()
   );
   public static final ToolMaterial STONE = register(
      ToolMaterial.builder("stone", 11383484).head(130, 4.0F, 1.0F, Tiers.STONE).trait(PartRole.HEAD, Trait.STONEBOUND, 1).build()
   );
   public static final ToolMaterial FLINT = register(
      ToolMaterial.builder("flint", 9342616).head(150, 5.0F, 1.25F, Tiers.STONE).trait(PartRole.HEAD, Trait.JAGGED, 1).build()
   );
   public static final ToolMaterial BONE = register(
      ToolMaterial.builder("bone", 16773581)
         .head(100, 4.5F, 1.25F, Tiers.STONE)
         .handle(1.1F, 1.0F, 1.05F)
         .trait(PartRole.HEAD, Trait.RESILIENT, 1)
         .trait(PartRole.HANDLE, Trait.RESILIENT, 1)
         .build()
   );
   public static final ToolMaterial BAMBOO = register(
      ToolMaterial.builder("bamboo", 12901752).handle(0.85F, 1.15F, 0.9F).trait(PartRole.HANDLE, Trait.LIGHTWEIGHT, 1).build()
   );
   public static final ToolMaterial BLAZE = register(
      ToolMaterial.builder("blaze", 16757820).handle(1.0F, 1.1F, 1.1F).trait(PartRole.HANDLE, Trait.BLAZING, 1).build()
   );
   public static final ToolMaterial COPPER = register(
      ToolMaterial.builder("copper", 16755320)
         .castable()
         .head(210, 5.0F, 1.0F, Tiers.STONE)
         .handle(1.05F, 1.0F, 1.0F)
         .trait(PartRole.HEAD, Trait.DWARVEN, 1)
         .build()
   );
   public static final ToolMaterial IRON = register(
      ToolMaterial.builder("iron", 15200504)
         .castable()
         .head(280, 6.0F, 2.0F, Tiers.IRON)
         .handle(1.35F, 0.95F, 1.05F)
         .trait(PartRole.HEAD, Trait.MAGNETIC, 1)
         .build()
   );
   public static final ToolMaterial GOLD = register(
      ToolMaterial.builder("gold", 16766044)
         .castable()
         .head(64, 12.0F, 0.0F, Tiers.GOLD)
         .handle(0.9F, 1.1F, 1.0F)
         .trait(PartRole.HEAD, Trait.LUSTROUS, 1)
         .trait(PartRole.HANDLE, Trait.LUSTROUS, 1)
         .build()
   );
   public static final ToolMaterial ROSE_GOLD = register(
      ToolMaterial.builder("rose_gold", 16759208)
         .castable()
         .head(200, 9.0F, 1.0F, Tiers.IRON)
         .handle(1.0F, 1.05F, 1.0F)
         .trait(PartRole.HEAD, Trait.ENHANCED, 1)
         .build()
   );
   public static final ToolMaterial AMETHYST_BRONZE = register(
      ToolMaterial.builder("amethyst_bronze", 14263280)
         .castable()
         .head(300, 6.5F, 1.5F, Tiers.IRON)
         .handle(1.1F, 1.0F, 1.05F)
         .trait(PartRole.HEAD, Trait.CRUMBLING, 1)
         .build()
   );
   public static final ToolMaterial DIAMOND = register(
      ToolMaterial.builder("diamond", 5633246).head(1561, 8.0F, 3.0F, Tiers.DIAMOND).trait(PartRole.HEAD, Trait.STURDY, 1).build()
   );
   public static final ToolMaterial NETHERITE = register(
      ToolMaterial.builder("netherite", 8746884)
         .castable()
         .head(2031, 9.0F, 4.0F, Tiers.NETHERITE)
         .handle(1.25F, 1.05F, 1.15F)
         .trait(PartRole.HEAD, Trait.FIREPROOF, 1)
         .trait(PartRole.HANDLE, Trait.FIREPROOF, 1)
         .build()
   );
   public static final ToolMaterial STRING = register(ToolMaterial.builder("string", 16774627).binding(0.0F, 0.0F).build());
   public static final ToolMaterial LEATHER = register(
      ToolMaterial.builder("leather", 13538662).binding(0.15F, 0.0F).trait(PartRole.BINDING, Trait.TANNED, 1).build()
   );
   public static final ToolMaterial HONEYCOMB = register(
      ToolMaterial.builder("honeycomb", 16760907).binding(0.0F, 0.05F).trait(PartRole.BINDING, Trait.WAXED, 1).build()
   );
   public static final ToolMaterial CHAIN = register(
      ToolMaterial.builder("chain", 9081763).binding(0.1F, 0.0F).trait(PartRole.BINDING, Trait.REINFORCED, 1).build()
   );

   private Materials() {
   }

   public static synchronized ToolMaterial register(ToolMaterial material) {
      if (REGISTRY.putIfAbsent(material.id(), material) != null) {
         throw new IllegalStateException("Duplicate WT Workshop material " + material.id());
      } else {
         return material;
      }
   }

   @Nullable
   public static ToolMaterial get(String id) {
      return REGISTRY.get(id);
   }

   public static Collection<ToolMaterial> all() {
      return Collections.unmodifiableCollection(REGISTRY.values());
   }

   public static int headDurability(ToolMaterial m) {
      return WorkshopConfig.headDurability(m);
   }

   public static float headSpeed(ToolMaterial m) {
      return WorkshopConfig.headSpeed(m);
   }
}
