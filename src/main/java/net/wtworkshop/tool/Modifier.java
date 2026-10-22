package net.wtworkshop.tool;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public enum Modifier {
   HASTE(Modifier.Slot.UPGRADE, 5, 16, ChatFormatting.RED, Map.of(Items.REDSTONE, 1, Items.REDSTONE_BLOCK, 9)),
   SHARPNESS(Modifier.Slot.UPGRADE, 5, 16, ChatFormatting.WHITE, Map.of(Items.QUARTZ, 1, Items.QUARTZ_BLOCK, 4)),
   REINFORCED(Modifier.Slot.UPGRADE, 5, 2, ChatFormatting.GRAY, Map.of(Items.OBSIDIAN, 1)),
   LUCK(Modifier.Slot.UPGRADE, 3, 32, ChatFormatting.AQUA, Map.of(Items.LAPIS_LAZULI, 1, Items.LAPIS_BLOCK, 9)),
   FIERY(Modifier.Slot.UPGRADE, 2, 12, ChatFormatting.GOLD, Map.of(Items.BLAZE_POWDER, 1)),
   KNOCKBACK(Modifier.Slot.UPGRADE, 2, 8, ChatFormatting.GREEN, Map.of(Items.SLIME_BALL, 1, Items.SLIME_BLOCK, 9)),
   MAGNETIC(Modifier.Slot.UPGRADE, 1, 1, ChatFormatting.WHITE, Map.of(Items.COMPASS, 1)),
   EXPANDED(Modifier.Slot.UPGRADE, 2, 4, ChatFormatting.AQUA, Map.of(Items.PISTON, 1), t -> t.isHeavy()),
   DIAMOND(Modifier.Slot.UPGRADE, 1, 1, ChatFormatting.AQUA, Map.of(Items.DIAMOND, 1)),
   EMERALD(Modifier.Slot.UPGRADE, 1, 1, ChatFormatting.GREEN, Map.of(Items.EMERALD, 1)),
   NETHERITE(Modifier.Slot.UPGRADE, 1, 1, ChatFormatting.RED, Map.of(Items.NETHERITE_INGOT, 1)),
   SILKY(Modifier.Slot.ABILITY, 1, 1, ChatFormatting.LIGHT_PURPLE, Map.of(Items.GOLDEN_APPLE, 1)),
   AUTOSMELT(Modifier.Slot.ABILITY, 1, 4, ChatFormatting.GOLD, Map.of(Items.BLAZE_ROD, 1), t -> !t.isMelee()),
   SIPHONING(Modifier.Slot.ABILITY,3,4,ChatFormatting.DARK_RED,Map.of(Items.GHAST_TEAR,1),t->t.isMelee()||t==ToolType.AXE||t==ToolType.BROAD_AXE),
   MENDING(Modifier.Slot.ABILITY, 1, 16, ChatFormatting.YELLOW, Map.of(Items.EXPERIENCE_BOTTLE, 1));

   public final Modifier.Slot slot;
   public final int maxLevel;
   public final int perLevel;
   public final ChatFormatting color;
   private final Map<Item, Integer> inputs;
   private final Predicate<ToolType> allowed;

   private Modifier(Modifier.Slot slot, int maxLevel, int perLevel, ChatFormatting color, Map<Item, Integer> inputs) {
      this(slot, maxLevel, perLevel, color, inputs, t -> true);
   }

   private Modifier(Modifier.Slot slot, int maxLevel, int perLevel, ChatFormatting color, Map<Item, Integer> inputs, Predicate<ToolType> allowed) {
      this.slot = slot;
      this.maxLevel = maxLevel;
      this.perLevel = perLevel;
      this.color = color;
      this.inputs = new LinkedHashMap<>(inputs);
      this.allowed = allowed;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public int maximumLevel(){return this==SIPHONING?net.wtworkshop.WorkshopConfig.siphoningMaxLevel():maxLevel;}

   public boolean allowedOn(ToolType type) {
      return this.allowed.test(type);
   }

   public int valueOf(ItemStack stack) {
      return this.inputs.getOrDefault(stack.getItem(), 0);
   }

   public Set<Item> inputItems() {
      return this.inputs.keySet();
   }

   public boolean conflictsWith(Modifier other) {
      return this == SILKY && (other == LUCK || other == AUTOSMELT) || other == SILKY && (this == LUCK || this == AUTOSMELT);
   }

   @Nullable
   public Modifier requires() {
      return this == NETHERITE ? DIAMOND : null;
   }

   public Component displayName(int level) {
      Component name = Component.translatable("modifier.wt_workshop." + this.id());
      return (this.maxLevel > 1
            ? Component.translatable("wt_workshop.level_suffix", new Object[]{name, Component.translatable("enchantment.level." + level)})
            : name.copy())
         .withStyle(this.color);
   }

   public Component description() {
      if(this==SIPHONING)return Component.translatable("modifier.wt_workshop.siphoning.desc",net.wtworkshop.WorkshopConfig.siphoningFraction()*100,net.wtworkshop.WorkshopConfig.siphoningCap(),net.wtworkshop.WorkshopConfig.siphoningCooldown()/20f).withStyle(ChatFormatting.DARK_GRAY);
      return Component.translatable("modifier.wt_workshop." + this.id() + ".desc").withStyle(ChatFormatting.DARK_GRAY);
   }

   @Nullable
   public static Modifier forIngredient(ItemStack stack) {
      for (Modifier m : values()) {
         if (m.valueOf(stack) > 0) {
            return m;
         }
      }

      return null;
   }

   @Nullable
   public static Modifier byId(String id) {
      for (Modifier m : values()) {
         if (m.id().equals(id)) {
            return m;
         }
      }

      return null;
   }

   public static enum Slot {
      UPGRADE,
      ABILITY;
   }
}
