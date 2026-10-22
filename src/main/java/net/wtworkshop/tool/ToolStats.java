package net.wtworkshop.tool;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.wtworkshop.WorkshopConfig;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.PartRole;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.material.Trait;

public record ToolStats(
   int durability,
   float speed,
   float attack,
   float attackSpeed,
   Tier tier,
   Map<Trait, Integer> traits,
   int upgradeSlots,
   int abilitySlots,
   int usedUpgrades,
   int usedAbilities
) {
   private static final List<Tiers> TIER_ORDER = List.of(Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE);
   private static final Map<ToolType, Map<ToolData, ToolStats>> CACHE = new EnumMap<>(ToolType.class);
   public static final ToolStats BROKEN_DATA = new ToolStats(1, 1.0F, 0.0F, 0.0F, Tiers.WOOD, Map.of(), 0, 0, 0, 0);

   public static synchronized ToolStats of(ToolType type, ToolData data) {
      return CACHE.computeIfAbsent(type, t -> Collections.synchronizedMap(new WeakHashMap<>())).computeIfAbsent(data, d -> compute(type, d));
   }

   public static synchronized void clearCache() {
      CACHE.clear();
   }

   public int trait(Trait t) {
      return this.traits.getOrDefault(t, 0);
   }

   public int freeUpgrades() {
      return this.upgradeSlots - this.usedUpgrades;
   }

   public int freeAbilities() {
      return this.abilitySlots - this.usedAbilities;
   }

   private static ToolStats compute(ToolType type, ToolData data) {
      if (!data.valid()) {
         return BROKEN_DATA;
      } else {
         ToolMaterial headMat = data.material(PartRole.HEAD);
         ToolMaterial handleMat = data.material(PartRole.HANDLE);
         ToolMaterial bindingMat = data.material(PartRole.BINDING);
         ToolMaterial.HeadStats head = headMat.head();
         ToolMaterial.HandleStats handle = handleMat.handle();
         ToolMaterial.BindingStats binding = bindingMat.binding();
         Map<Trait, Integer> traits = new EnumMap<>(Trait.class);
         headMat.traits(PartRole.HEAD).forEach((t, l) -> traits.merge(t, l, Integer::sum));
         handleMat.traits(PartRole.HANDLE).forEach((t, l) -> traits.merge(t, l, Integer::sum));
         bindingMat.traits(PartRole.BINDING).forEach((t, l) -> traits.merge(t, l, Integer::sum));
         int diamond = data.modifierLevel(Modifier.DIAMOND);
         int emerald = data.modifierLevel(Modifier.EMERALD);
         int netherite = data.modifierLevel(Modifier.NETHERITE);
         double levelBonus = 1.0 + (double)data.level() * WorkshopConfig.levelBonus();
         double durability = (double)Materials.headDurability(headMat) * (emerald > 0 ? 1.5 : 1.0);
         durability *= (double)handle.durability() * (1.0 + (double)binding.durability()) * (double)type.durabilityScale;
         durability += (double)(diamond * 500);
         durability *= (netherite > 0 ? 1.2 : 1.0) * levelBonus;
         double speed = (double)(Materials.headSpeed(headMat) * handle.speed()) * (1.0 + (double)binding.speed()) * (double)type.speedScale;
         speed *= 1.0 + 0.2 * (double)data.modifierLevel(Modifier.HASTE);
         speed += (double)diamond * 0.5;
         speed *= (netherite > 0 ? 1.1 : 1.0) * levelBonus;
         double attack = (double)((type.baseAttack + head.attack() * type.attackScale) * handle.attack());
         attack += 0.75 * (double)data.modifierLevel(Modifier.SHARPNESS) + 0.5 * (double)diamond + 0.5 * (double)netherite;
         int tierRank = rank(head.tier());
         if (emerald > 0) {
            tierRank = Math.max(tierRank, 2);
         }

         if (diamond > 0) {
            tierRank = Math.max(tierRank, 3);
         }

         if (netherite > 0) {
            tierRank = 4;
         }

         Tier tier = (Tier)(head.tier() == Tiers.GOLD && tierRank == 0 ? Tiers.GOLD : (Tier)TIER_ORDER.get(tierRank));
         int level = Math.min(data.level(), WorkshopConfig.maxLevel());
         int upgrades = WorkshopConfig.baseUpgradeSlots() + level + traits.getOrDefault(Trait.ENHANCED, 0) + data.expansionTier()*WorkshopConfig.expansionUpgradeSlots();
         int abilities = WorkshopConfig.baseAbilitySlots() + (level >= 5 ? 1 : 0) + (level >= 10 ? 1 : 0) + (data.expansionTier()==3?WorkshopConfig.finalExpansionAbilitySlots():0);
         int usedUpgrades = 0;
         int usedAbilities = 0;

         for (String id : data.modifiers()) {
            Modifier m = Modifier.byId(id);
            if (m == null) {
               if ("ABILITY".equals(data.addonSlots().get(id))) usedAbilities++; else usedUpgrades++;
            } else {
               if (m.slot == Modifier.Slot.UPGRADE) {
                  usedUpgrades++;
               } else {
                  usedAbilities++;
               }
            }
         }

         return new ToolStats(
            Math.max(1, (int)Math.round(durability)),
            (float)speed,
            (float)attack,
            type.attackSpeed,
            tier,
            Collections.unmodifiableMap(traits),
            upgrades,
            abilities,
            usedUpgrades,
            usedAbilities
         );
      }
   }

   private static int rank(Tier tier) {
      if (tier == Tiers.GOLD) {
         return 0;
      } else {
         int i = TIER_ORDER.indexOf(tier);
         return i < 0 ? 2 : i;
      }
   }

   public float spareChance(ToolData data) {
      int reinforced = data.modifierLevel(Modifier.REINFORCED) + this.trait(Trait.REINFORCED);
      float base=Math.min(0.9F,0.1F*this.trait(Trait.RESILIENT)+0.1F*this.trait(Trait.STURDY)+0.15F*reinforced);
      return Math.min(0.9F,1-(1-base)*net.wtworkshop.api.AddonModifiers.remainingWearFactor(data));
   }

   public boolean fireproof(ToolData data) {
      return this.trait(Trait.FIREPROOF) > 0 || data.modifierLevel(Modifier.NETHERITE) > 0;
   }

   public boolean magnetic(ToolData data) {
      return this.trait(Trait.MAGNETIC) > 0 || data.modifierLevel(Modifier.MAGNETIC) > 0;
   }
}
