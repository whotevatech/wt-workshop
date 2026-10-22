package net.wtworkshop.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.wtworkshop.smeltery.Molten;
import net.wtworkshop.smeltery.SmelteryMenu;
import net.wtworkshop.util.Text;

public class SmelteryScreen extends AbstractContainerScreen<SmelteryMenu> {
   private static final int TANK_X = 50;
   private static final int TANK_Y = 28;
   private static final int TANK_W = 78;
   private static final int TANK_H = 96;

   public SmelteryScreen(SmelteryMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 222;
      this.inventoryLabelY = 129;
   }

   private List<SmelteryScreen.Segment> segments() {
      List<SmelteryScreen.Segment> out = new ArrayList<>();
      int count = Math.min(((SmelteryMenu)this.menu).data(9), 8);
      int capacity = Math.max(1, ((SmelteryMenu)this.menu).data(26));
      int bottom = this.topPos + 28 + 96 - 1;

      for (int i = 0; i < count; i++) {
         Molten m = Molten.byIndex(((SmelteryMenu)this.menu).data(10 + i * 2));
         int amount = ((SmelteryMenu)this.menu).data(10 + i * 2 + 1);
         if (m != null && amount > 0) {
            int h = Math.max(2, Math.round((float)amount / (float)capacity * 94.0F));
            int top = Math.max(this.topPos + 28 + 1, bottom - h);
            out.add(new SmelteryScreen.Segment(i, m, amount, top, bottom));
            bottom = top;
         }
      }

      return out;
   }

   protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      Gui.frame(g, x, y, this.imageWidth, this.imageHeight);

      for (Slot slot : ((SmelteryMenu)this.menu).slots) {
         Gui.slot(g, x + slot.x, y + slot.y, slot.index == 5);
      }

      for (int i = 0; i < 3; i++) {
         int max = ((SmelteryMenu)this.menu).data(6 + i);
         float f = max > 0 ? (float)((SmelteryMenu)this.menu).data(3 + i) / (float)max : 0.0F;
         int[] p = SmelteryMenu.POSITIONS[i];
         verticalBar(g, x + p[0] + 19, y + p[1], 5, 16, f, -1531840);
      }

      int burnMax = ((SmelteryMenu)this.menu).data(1);
      int[] fuel = SmelteryMenu.POSITIONS[3];
      verticalBar(g, x + fuel[0] + 19, y + fuel[1], 5, 16, burnMax > 0 ? (float)((SmelteryMenu)this.menu).data(0) / (float)burnMax : 0.0F, -38358);
      int[] cast = SmelteryMenu.POSITIONS[4];
      verticalBar(g, x + cast[0] + 5, y + cast[1] + 20, 6, 18, (float)((SmelteryMenu)this.menu).data(2) / 40.0F, -14286);
      Gui.box(g, x + 50, y + 28, 78, 96, -15066082, -6651554);

      for (SmelteryScreen.Segment s : this.segments()) {
         int color = s.molten.color();
         g.fill(x + 50 + 1, s.top, x + 50 + 78 - 1, s.bottom, color);
         g.fill(x + 50 + 1, s.top, x + 50 + 78 - 1, s.top + 1, brighten(color));
         if (s.index == 0) {
            g.fill(x + 50 + 1, s.top, x + 50 + 3, s.bottom, -1);
         }
      }

      for (int gy = y + 28 + 12; gy < y + 28 + 96 - 2; gy += 12) {
         g.fill(x + 50 + 78 - 7, gy, x + 50 + 78 - 1, gy + 1, -2130706433);
      }
   }

   private static int brighten(int argb) {
      int r = Math.min(255, (argb >> 16 & 0xFF) + 50);
      int gg = Math.min(255, (argb >> 8 & 0xFF) + 50);
      int b = Math.min(255, (argb & 0xFF) + 50);
      return 0xFF000000 | r << 16 | gg << 8 | b;
   }

   private static void verticalBar(GuiGraphics g, int x, int y, int w, int h, float fraction, int color) {
      Gui.box(g, x, y, w, h, -3820145, -6651554);
      int fill = Math.round((float)(h - 2) * Math.max(0.0F, Math.min(1.0F, fraction)));
      if (fill > 0) {
         g.fill(x + 1, y + h - 1 - fill, x + w - 1, y + h - 1, color);
      }
   }

   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      Gui.text(g, this.font, this.title.getString().toUpperCase(Locale.ROOT), 14, 10, -10338273);
      int capacity = Math.max(1, ((SmelteryMenu)this.menu).data(26));
      int stored = 0;

      for (SmelteryScreen.Segment s : this.segments()) {
         stored += s.amount;
      }

      String fill = Text.number((double)stored / 90.0) + " / " + Text.number((double)capacity / 90.0);
      Gui.text(g, this.font, fill, this.imageWidth - 12 - this.font.width(fill), 10, -9412532);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.cast"), 136, 40, -13161440);
      Gui.text(g, this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, -13161440);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
      super.render(g, mouseX, mouseY, partial);
      this.renderTooltip(g, mouseX, mouseY);
      SmelteryScreen.Segment hovered = this.hovered((double)mouseX, (double)mouseY);
      if (hovered != null) {
         List<Component> lines = new ArrayList<>();
         lines.add(hovered.molten.displayName().copy().withStyle(ChatFormatting.WHITE));
         lines.add(
            Component.translatable("screen.wt_workshop.smeltery.amount", new Object[]{Text.number((double)hovered.amount / 90.0), hovered.amount})
               .withStyle(ChatFormatting.GRAY)
         );
         lines.add(
            Component.translatable(hovered.index == 0 ? "screen.wt_workshop.smeltery.pouring" : "screen.wt_workshop.smeltery.click_select")
               .withStyle(hovered.index == 0 ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY)
         );
         lines.add(Component.translatable("screen.wt_workshop.smeltery.shift_dump").withStyle(ChatFormatting.DARK_GRAY));
         g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
      } else if (mouseX >= this.leftPos + 50 && mouseX < this.leftPos + 50 + 78 && mouseY >= this.topPos + 28 && mouseY < this.topPos + 28 + 96) {
         g.renderTooltip(this.font, Component.translatable("screen.wt_workshop.smeltery.empty"), mouseX, mouseY);
      }
   }

   private SmelteryScreen.Segment hovered(double mouseX, double mouseY) {
      if (!(mouseX < (double)(this.leftPos + 50)) && !(mouseX >= (double)(this.leftPos + 50 + 78))) {
         for (SmelteryScreen.Segment s : this.segments()) {
            if (mouseY >= (double)s.top && mouseY < (double)s.bottom) {
               return s;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      SmelteryScreen.Segment s = this.hovered(mouseX, mouseY);
      if (s != null && button == 0) {
         int id = Screen.hasShiftDown() ? 100 + s.index : s.index;
         this.minecraft.gameMode.handleInventoryButtonClick(((SmelteryMenu)this.menu).containerId, id);
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private static record Segment(int index, Molten molten, int amount, int top, int bottom) {
   }
}
