package net.wtworkshop.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.wtworkshop.Registration;
import net.wtworkshop.builder.PartBuilderLogic;
import net.wtworkshop.builder.PartBuilderMenu;
import net.wtworkshop.material.MaterialRecipe;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.part.PartType;
import net.wtworkshop.part.PatternItem;

public class BuilderScreen extends AbstractContainerScreen<PartBuilderMenu> {
   private static final int GRID_X = 188;
   private static final int GRID_Y = 54;
   private static final int CELL = 20;
   private static final int COLUMNS = 6;

   public BuilderScreen(PartBuilderMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 326;
      this.imageHeight = 224;
      this.inventoryLabelY = 128;
   }

   protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      Gui.frame(g, x, y, this.imageWidth, this.imageHeight);
      Gui.box(g, x + 8, y + 28, 164, 87, -1384507, -6651554);
      Gui.box(g, x + 180, y + 28, 138, 188, -2109523, -6651554);

      for (Slot slot : ((PartBuilderMenu)this.menu).slots) {
         Gui.slot(g, x + slot.x, y + slot.y, slot.index == 0);
      }

      if (((PartBuilderMenu)this.menu).carving()) {
         PartType[] types = PartType.values();

         for (int i = 0; i < types.length; i++) {
            int cx = x + 188 + i % 6 * 20;
            int cy = y + 54 + i / 6 * 20;
            boolean hover = mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18;
            Gui.box(
               g,
               cx,
               cy,
               18,
               18,
               i == ((PartBuilderMenu)this.menu).selected() ? -2507389 : (hover ? -1187911 : -5990530),
               i == ((PartBuilderMenu)this.menu).selected() ? -10338273 : -9609137
            );
            g.renderItem(new ItemStack((ItemLike)Registration.PATTERNS.get(types[i]).get()), cx + 1, cy + 1);
         }
      }
   }

   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      Gui.text(g, this.font, Component.translatable("block.wt_workshop.part_builder").getString().toUpperCase(Locale.ROOT), 16, 10, -10338273);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.builder.subtitle"), 192, 10, -9412532);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.pattern"), 15, 40, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.material"), 67, 40, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.part"), 139, 40, -10338273);
      Gui.text(g, this.font, ">", 116, 61, -10338273);
      Gui.text(g, this.font, this.playerInventoryTitle, 10, this.inventoryLabelY, -13161440);
      PartBuilderLogic.Outcome outcome = ((PartBuilderMenu)this.menu).compute();
      int color = outcome.success() ? -13995976 : -9412532;
      if (outcome.message() != null) {
         Gui.paragraph(g, this.font, outcome.message(), 15, 82, 150, color, 110);
      }

      int px = 188;
      if (((PartBuilderMenu)this.menu).carving()) {
         Gui.text(g, this.font, Component.translatable("screen.wt_workshop.builder.choose"), px, 37, -10338273);
         int rows = (PartType.values().length + 6 - 1) / 6;
         int y = 54 + rows * 20 + 6;
         if (((PartBuilderMenu)this.menu).selected() >= 0) {
            PartType type = PartType.values()[((PartBuilderMenu)this.menu).selected()];
            Gui.text(g, this.font, type.displayName(), px, y, -13161440);
            Gui.text(g, this.font, Component.translatable("tooltip.wt_workshop.pattern.cost", new Object[]{type.cost}), px, y + 12, -9412532);
         }
      } else {
         Gui.text(g, this.font, Component.translatable("screen.wt_workshop.builder.preview"), px, 37, -10338273);
         ItemStack pattern = ((PartBuilderMenu)this.menu).pattern();
         Optional<MaterialRecipe> recipe = MaterialRecipe.find(((PartBuilderMenu)this.menu).player().level(), ((PartBuilderMenu)this.menu).material());
         if (!(pattern.getItem() instanceof PatternItem p)) {
            Gui.paragraph(g, this.font, Component.translatable("screen.wt_workshop.builder.hint"), px, 54, 124, -9412532, 210);
         } else {
            int var18 = 54;
            Gui.text(g, this.font, p.type.displayName(), px, var18, -13161440);
            var18 += 12;
            ToolMaterial m = recipe.map(MaterialRecipe::toolMaterial).orElse(null);
            if (m != null && m.supports(p.type.role)) {
               Gui.text(g, this.font, m.displayName(), px, var18, Gui.readable(m.color()));
               var18 += 14;
               List<Component> lines = new ArrayList<>();
               PartItem.statLines(m, p.type, lines);
               m.traits(p.type.role).forEach((t, l) -> lines.add(t.displayName(l)));

               for (Component line : lines) {
                  var18 = Gui.paragraph(g, this.font, line, px, var18, 124, -13161440, 210);
               }
            } else {
               Gui.paragraph(
                  g, this.font, Component.translatable("screen.wt_workshop.builder.need_material", new Object[]{p.type.cost}), px, var18, 124, -9412532, 210
               );
            }
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (((PartBuilderMenu)this.menu).carving()) {
         for (int i = 0; i < PartType.values().length; i++) {
            int cx = this.leftPos + 188 + i % 6 * 20;
            int cy = this.topPos + 54 + i / 6 * 20;
            if (mouseX >= (double)cx
               && mouseX < (double)(cx + 18)
               && mouseY >= (double)cy
               && mouseY < (double)(cy + 18)
               && ((PartBuilderMenu)this.menu).clickMenuButton(((PartBuilderMenu)this.menu).player(), i)) {
               this.minecraft.gameMode.handleInventoryButtonClick(((PartBuilderMenu)this.menu).containerId, i);
               return true;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
      super.render(g, mouseX, mouseY, partial);
      this.renderTooltip(g, mouseX, mouseY);
      if (((PartBuilderMenu)this.menu).carving()) {
         for (int i = 0; i < PartType.values().length; i++) {
            int cx = this.leftPos + 188 + i % 6 * 20;
            int cy = this.topPos + 54 + i / 6 * 20;
            if (mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18) {
               g.renderTooltip(this.font, PartType.values()[i].displayName(), mouseX, mouseY);
            }
         }
      }
   }
}
