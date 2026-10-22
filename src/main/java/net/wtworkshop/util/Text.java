package net.wtworkshop.util;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.wtworkshop.client.ClientHooks;

public final class Text {
   private Text() {
   }

   public static String number(double value) {
      String s = String.format(Locale.ROOT, "%.2f", value);
      s = s.replaceAll("0+$", "");
      return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
   }

   public static String multiplier(float value) {
      return "×" + number((double)value);
   }

   public static String percent(float value) {
      int p = Math.round(value * 100.0F);
      return (p >= 0 ? "+" : "") + p + "%";
   }

   public static MutableComponent stat(String key, String value) {
      return stat(key, Component.literal(value));
   }

   public static MutableComponent stat(String key, Component value) {
      return Component.translatable("stat.wt_workshop." + key)
         .withStyle(ChatFormatting.GRAY)
         .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
         .append(value.copy().withStyle(ChatFormatting.WHITE));
   }

   public static MutableComponent indent(Component c) {
      return Component.literal("  ").append(c);
   }

   public static Component holdShift() {
      return Component.translatable("tooltip.wt_workshop.hold_shift").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC});
   }

   public static boolean shiftDown() {
      return FMLEnvironment.dist.isClient() && ClientHooks.shiftDown();
   }
}
