package net.wtworkshop.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class Gui {
   public static final int INK = -13161440;
   public static final int MUTED = -9412532;
   public static final int ACCENT = -10338273;
   public static final int GOOD = -13995976;
   public static final int BAD = -6670292;
   public static final int FRAME_OUT = -11980257;
   public static final int FRAME = -9088457;
   public static final int FRAME_LINE = -5733545;
   public static final int PANEL = -1384507;
   public static final int PANEL_DARK = -2109523;
   public static final int PANEL_LINE = -6651554;
   public static final int HEADER = -2703985;

   private Gui() {
   }

   public static int readable(int argb) {
      int r=argb>>16&255,g=argb>>8&255,b=argb&255;
      while(.299*r+.587*g+.114*b>105){r=r*9/10;g=g*9/10;b=b*9/10;}
      return 0xff000000|r<<16|g<<8|b;
   }

   public static void box(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
      g.fill(x, y, x + w, y + h, border);
      g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
   }

   public static void frame(GuiGraphics g, int x, int y, int w, int h) {
      box(g, x, y, w, h, -9088457, -11980257);
      box(g, x + 3, y + 3, w - 6, h - 6, -3162726, -5733545);
      g.fill(x + 5, y + 5, x + w - 5, y + 24, -2703985);

      for (int cx : new int[]{x + 7, x + w - 10}) {
         for (int cy : new int[]{y + 8, y + h - 11}) {
            g.fill(cx, cy, cx + 3, cy + 3, -2638713);
         }
      }
   }

   public static void slot(GuiGraphics g, int x, int y, boolean highlight) {
      box(g, x - 1, y - 1, 18, 18, -5990530, highlight ? -5080267 : -9609137);
      g.fill(x, y, x + 16, y + 1, -8950435);
      g.fill(x, y + 15, x + 16, y + 16, -792108);
   }

   private static Component readableText(Component source) {
      var result = Component.empty();
      source.visit((style, text) -> {
         result.append(Component.literal(text).setStyle(style.getColor() == null ? style : style.withColor(readable(style.getColor().getValue()))));
         return java.util.Optional.empty();
      }, net.minecraft.network.chat.Style.EMPTY);
      return result;
   }

   public static void text(GuiGraphics g, Font font, Component s, int x, int y, int color) {
      g.drawString(font, readableText(s), x, y, color, false);
   }

   public static void text(GuiGraphics g, Font font, String s, int x, int y, int color) {
      g.drawString(font, s, x, y, readable(color), false);
   }

   public static int paragraph(GuiGraphics g, Font font, Component s, int x, int y, int width, int color, int maxY) {
      for (FormattedCharSequence line : font.split(readableText(s), width)) {
         if (y > maxY) {
            break;
         }

         g.drawString(font, line, x, y, color, false);
         y += 10;
      }

      return y;
   }

   public static void bar(GuiGraphics g, int x, int y, int w, int h, float fraction, int color) {
      box(g, x, y, w, h, -3820145, -6651554);
      int fill = Math.round((float)(w - 2) * Math.max(0.0F, Math.min(1.0F, fraction)));
      if (fill > 0) {
         g.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, color);
      }
   }
}
