package net.wtworkshop.smeltery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.wtworkshop.Registration;
import org.jetbrains.annotations.Nullable;

public record Molten(String id, int color, @Nullable String material, Supplier<Item> ingot, @Nullable Supplier<Item> block, int blockAmount) {
   public static final int INGOT = 90;
   private static final List<Molten> REGISTRY = new ArrayList<>();
   public static final Molten IRON = register(new Molten("iron", -2056070, "iron", () -> Items.IRON_INGOT, () -> Items.IRON_BLOCK, 810));
   public static final Molten GOLD = register(new Molten("gold", -14286, "gold", () -> Items.GOLD_INGOT, () -> Items.GOLD_BLOCK, 810));
   public static final Molten COPPER = register(new Molten("copper", -882101, "copper", () -> Items.COPPER_INGOT, () -> Items.COPPER_BLOCK, 810));
   public static final Molten NETHERITE = register(
      new Molten("netherite", -10862264, "netherite", () -> Items.NETHERITE_INGOT, () -> Items.NETHERITE_BLOCK, 810)
   );
   public static final Molten DEBRIS = register(new Molten("debris", -8761542, null, () -> Items.NETHERITE_SCRAP, null, 0));
   public static final Molten ROSE_GOLD = register(new Molten("rose_gold", -25462, "rose_gold", Registration.ROSE_GOLD_INGOT, null, 0));
   public static final Molten AMETHYST_BRONZE = register(
      new Molten("amethyst_bronze", -3568920, "amethyst_bronze", Registration.AMETHYST_BRONZE_INGOT, null, 0)
   );
   public static final Molten AMETHYST = register(new Molten("amethyst", -5800982, null, () -> Items.AMETHYST_SHARD, () -> Items.AMETHYST_BLOCK, 360));

   public static Molten register(Molten molten) {
      if (get(molten.id) != null) {
         throw new IllegalStateException("Duplicate molten " + molten.id);
      } else {
         REGISTRY.add(molten);
         return molten;
      }
   }

   @Nullable
   public static Molten get(String id) {
      for (Molten m : REGISTRY) {
         if (m.id.equals(id)) {
            return m;
         }
      }

      return null;
   }

   public int index() {
      return REGISTRY.indexOf(this);
   }

   @Nullable
   public static Molten byIndex(int index) {
      return index >= 0 && index < REGISTRY.size() ? REGISTRY.get(index) : null;
   }

   public static List<Molten> all() {
      return Collections.unmodifiableList(REGISTRY);
   }

   @Nullable
   public static Molten forMaterial(String material) {
      for (Molten m : REGISTRY) {
         if (material.equals(m.material)) {
            return m;
         }
      }

      return null;
   }

   public Component displayName() {
      int colon = this.id.indexOf(58);
      return Component.translatable(colon < 0 ? "molten.wt_workshop." + this.id : "molten." + this.id.substring(0, colon) + "." + this.id.substring(colon + 1));
   }
}
