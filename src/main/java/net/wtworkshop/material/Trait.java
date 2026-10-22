package net.wtworkshop.material;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum Trait {
   ECOLOGICAL(ChatFormatting.GREEN),
   STONEBOUND(ChatFormatting.GRAY),
   JAGGED(ChatFormatting.GRAY),
   RESILIENT(ChatFormatting.WHITE),
   DWARVEN(ChatFormatting.GOLD),
   MAGNETIC(ChatFormatting.AQUA),
   LUSTROUS(ChatFormatting.YELLOW),
   ENHANCED(ChatFormatting.LIGHT_PURPLE),
   CRUMBLING(ChatFormatting.LIGHT_PURPLE),
   STURDY(ChatFormatting.AQUA),
   FIREPROOF(ChatFormatting.RED),
   LIGHTWEIGHT(ChatFormatting.GREEN),
   BLAZING(ChatFormatting.GOLD),
   TANNED(ChatFormatting.GOLD),
   WAXED(ChatFormatting.YELLOW),
   REINFORCED(ChatFormatting.GRAY);

   public final ChatFormatting color;

   private Trait(ChatFormatting color) {
      this.color = color;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public Component displayName(int level) {
      Component name = Component.translatable("trait.wt_workshop." + this.id());
      return level > 1
         ? Component.translatable("wt_workshop.level_suffix", new Object[]{name, Component.translatable("enchantment.level." + level)}).withStyle(this.color)
         : name.copy().withStyle(this.color);
   }

   public Component description() {
      return Component.translatable("trait.wt_workshop." + this.id() + ".desc").withStyle(ChatFormatting.DARK_GRAY);
   }
}
