package net.wtworkshop;

import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.ToolMaterial;

public final class WorkshopConfig {
   public static final ModConfigSpec SPEC;
   private static final IntValue BASE_UPGRADES;
   private static final IntValue BASE_ABILITIES;
   private static final IntValue MAX_LEVEL;
   private static final IntValue XP_BASE;
   private static final IntValue XP_GROWTH;
   private static final DoubleValue LEVEL_BONUS;
   private static final DoubleValue ORE_MULTIPLIER;
   private static final IntValue TANK_CAPACITY;
   private static final IntValue MAX_TREE_LOGS;
   private static final IntValue EXPANSION_TIERS, EXPANSION_UPGRADES, FINAL_EXPANSION_ABILITIES, SIPHONING_MAX_LEVEL, SIPHONING_COOLDOWN;
   private static final DoubleValue SIPHONING_FRACTION, SIPHONING_CAP;
   private static final ModConfigSpec.BooleanValue SIPHONING_PVP;
   private static final Map<String, IntValue> DURABILITY = new LinkedHashMap<>();
   private static final Map<String, DoubleValue> SPEED = new LinkedHashMap<>();

   private WorkshopConfig() {
   }

   private static boolean loaded() {
      return SPEC.isLoaded();
   }

   public static int baseUpgradeSlots() {
      return loaded() ? (Integer)BASE_UPGRADES.get() : (Integer)BASE_UPGRADES.getDefault();
   }

   public static int baseAbilitySlots() {
      return loaded() ? (Integer)BASE_ABILITIES.get() : (Integer)BASE_ABILITIES.getDefault();
   }

   public static int maxLevel() {
      return loaded() ? (Integer)MAX_LEVEL.get() : (Integer)MAX_LEVEL.getDefault();
   }

   public static int xpBase() {
      return loaded() ? (Integer)XP_BASE.get() : (Integer)XP_BASE.getDefault();
   }

   public static int xpGrowth() {
      return loaded() ? (Integer)XP_GROWTH.get() : (Integer)XP_GROWTH.getDefault();
   }

   public static double levelBonus() {
      return loaded() ? (Double)LEVEL_BONUS.get() : (Double)LEVEL_BONUS.getDefault();
   }

   public static double oreMultiplier() {
      return loaded() ? (Double)ORE_MULTIPLIER.get() : (Double)ORE_MULTIPLIER.getDefault();
   }

   public static int tankCapacity() {
      return loaded() ? (Integer)TANK_CAPACITY.get() : (Integer)TANK_CAPACITY.getDefault();
   }

   public static int maxTreeLogs() {
      return loaded() ? (Integer)MAX_TREE_LOGS.get() : (Integer)MAX_TREE_LOGS.getDefault();
   }

   public static int expansionTierLimit(){return loaded()?EXPANSION_TIERS.get():EXPANSION_TIERS.getDefault();}
   public static int expansionUpgradeSlots(){return loaded()?EXPANSION_UPGRADES.get():EXPANSION_UPGRADES.getDefault();}
   public static int finalExpansionAbilitySlots(){return loaded()?FINAL_EXPANSION_ABILITIES.get():FINAL_EXPANSION_ABILITIES.getDefault();}
   public static int siphoningMaxLevel(){return loaded()?SIPHONING_MAX_LEVEL.get():SIPHONING_MAX_LEVEL.getDefault();}
   public static int siphoningCooldown(){return loaded()?SIPHONING_COOLDOWN.get():SIPHONING_COOLDOWN.getDefault();}
   public static float siphoningFraction(){return (float)(loaded()?SIPHONING_FRACTION.get():SIPHONING_FRACTION.getDefault()).doubleValue();}
   public static float siphoningCap(){return (float)(loaded()?SIPHONING_CAP.get():SIPHONING_CAP.getDefault()).doubleValue();}
   public static boolean siphoningPvp(){return loaded()?SIPHONING_PVP.get():SIPHONING_PVP.getDefault();}

   public static int headDurability(ToolMaterial m) {
      IntValue v = DURABILITY.get(m.id());
      if (v == null) {
         return m.head() == null ? 1 : m.head().durability();
      } else {
         return loaded() ? (Integer)v.get() : (Integer)v.getDefault();
      }
   }

   public static float headSpeed(ToolMaterial m) {
      DoubleValue v = SPEED.get(m.id());
      if (v == null) {
         return m.head() == null ? 1.0F : m.head().speed();
      } else {
         return (float)(loaded() ? (Double)v.get() : (Double)v.getDefault()).doubleValue();
      }
   }

   static {
      Builder b = new Builder();
      b.push("progression");
      BASE_UPGRADES = b.comment("Upgrade slots on a freshly assembled tool.").defineInRange("baseUpgradeSlots", 3, 0, 32);
      BASE_ABILITIES = b.comment("Ability slots on a freshly assembled tool. Tools gain one more at level 5 and level 10.")
         .defineInRange("baseAbilitySlots", 1, 0, 8);
      MAX_LEVEL = b.comment("Highest tool level. Each level gives one upgrade slot.").defineInRange("maximumLevel", 10, 0, 100);
      XP_BASE = b.comment("Tool XP needed for level 1.").defineInRange("firstLevelXp", 128, 1, 1000000);
      XP_GROWTH = b.comment("Extra XP needed for each level after the first.").defineInRange("additionalXpPerLevel", 64, 0, 1000000);
      LEVEL_BONUS = b.comment("Durability and mining speed bonus per tool level, as a fraction (0.02 = 2%).").defineInRange("levelStatBonus", 0.02, 0.0, 1.0);
      b.pop();
      b.push("expansions");
      EXPANSION_TIERS=b.comment("Highest expansion tier that may be newly applied. 0 disables new expansions; paid tiers are retained.").defineInRange("maximumTier",3,0,3);
      EXPANSION_UPGRADES=b.comment("Upgrade slots granted per applied expansion tier.").defineInRange("upgradeSlotsPerTier",1,0,8);
      FINAL_EXPANSION_ABILITIES=b.comment("Extra ability slots granted by Astral expansion (tier 3).").defineInRange("astralAbilitySlots",1,0,8);
      b.pop();b.push("siphoning");
      SIPHONING_MAX_LEVEL=b.comment("Maximum Siphoning level. 0 disables applying and using it.").defineInRange("maximumLevel",3,0,5);
      SIPHONING_FRACTION=b.comment("Fraction of actual melee health damage restored per Siphoning level.").defineInRange("healingFractionPerLevel",0.05,0.0,1.0);
      SIPHONING_CAP=b.comment("Maximum healing per successful proc, in health points (2 = one heart).").defineInRange("healingCap",2.0,0.0,20.0);
      SIPHONING_COOLDOWN=b.comment("Ticks between healing procs, shared across held weapons.").defineInRange("cooldownTicks",10,0,200);
      SIPHONING_PVP=b.comment("Allow Siphoning to heal from damaging other players.").define("affectsPlayers",false);
      b.pop();
      b.push("smeltery");
      ORE_MULTIPLIER = b.comment("How much molten metal ores and raw ores give, relative to an ingot.").defineInRange("oreMultiplier", 2.0, 1.0, 8.0);
      TANK_CAPACITY = b.comment("Smeltery tank size in units (90 units = 1 ingot).").defineInRange("tankCapacity", 2880, 810, 30000);
      b.pop();
      b.push("tools");
      MAX_TREE_LOGS = b.comment("Most logs a Broad Axe fells at once.").defineInRange("maxTreeLogs", 192, 1, 2048);
      b.pop();
      b.push("materials");

      for (ToolMaterial m : Materials.all()) {
         if (m.head() != null && !m.id().contains(":")) {
            b.push(m.id());
            DURABILITY.put(m.id(), b.defineInRange("durability", m.head().durability(), 1, 1000000));
            SPEED.put(m.id(), b.defineInRange("miningSpeed", (double)m.head().speed(), 0.1, 100.0));
            b.pop();
         }
      }

      b.pop();
      SPEC = b.build();
   }
}
