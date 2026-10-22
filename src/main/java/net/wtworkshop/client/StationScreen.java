package net.wtworkshop.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.wtworkshop.station.StationLogic;
import net.wtworkshop.station.StationMenu;
import net.wtworkshop.tool.Modifier;
import net.wtworkshop.tool.ToolData;
import net.wtworkshop.tool.ToolStats;
import net.wtworkshop.tool.WorkshopTool;
import net.wtworkshop.util.Text;

public class StationScreen extends AbstractContainerScreen<StationMenu> {
   public StationScreen(StationMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 402;
      this.imageHeight = 236;
      this.inventoryLabelY = 140;
   }

   protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      Gui.frame(g, x, y, this.imageWidth, this.imageHeight);
      Gui.box(g, x + 8, y + 28, 166, 104, -1384507, -6651554);
      Gui.box(g, x + 184, y + 28, 210, 202, -2109523, -6651554);

      for (Slot slot : ((StationMenu)this.menu).slots) {
         Gui.slot(g, x + slot.x, y + slot.y, slot.index == 0);
      }
   }

   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      Gui.text(g, this.font, Component.translatable("block.wt_workshop.tool_station").getString().toUpperCase(Locale.ROOT), 16, 10, -10338273);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.station.subtitle"), 286, 10, -9412532);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.head"), 21, 38, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.handle"), 68, 38, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.binding"), 116, 38, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.station.refit"), 14, 75, -9412532);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.tool"), 23, 90, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.material"), 64, 90, -13161440);
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.slot.result"), 138, 90, -10338273);
      Gui.text(g, this.font, ">", 116, 106, -10338273);
      Gui.text(g, this.font, this.playerInventoryTitle, 10, this.inventoryLabelY, -13161440);
      ItemStack before = ((StationMenu)this.menu).tool();
      ItemStack after = ((StationMenu)this.menu).preview();
      ItemStack shown = WorkshopTool.data(after) != null ? after : before;
      StationLogic.Outcome outcome = StationLogic.compute(((StationMenu)this.menu).inputList(), ((StationMenu)this.menu).player().level());
      int px = 192;
      Gui.text(g, this.font, Component.translatable("screen.wt_workshop.station.preview"), px, 37, -10338273);
      if (WorkshopTool.data(shown) == null) {
         Component hint = (Component)(outcome.message() != null ? outcome.message() : Component.translatable("screen.wt_workshop.station.hint"));
         int y = Gui.paragraph(g, this.font, hint, px, 55, 194, outcome.message() != null ? -6670292 : -9412532, 210);
         Gui.paragraph(g, this.font, Component.translatable("screen.wt_workshop.station.tip"), px, y + 10, 150, -9412532, 220);
      } else {
         Gui.text(g, this.font, this.font.plainSubstrByWidth(shown.getHoverName().getString(), 194), px, 51, -13161440);
         Gui.text(g, this.font, Component.translatable("screen.wt_workshop.now"), 300, 64, -9412532);
         Gui.text(g, this.font, Component.translatable("screen.wt_workshop.after"), 350, 64, -10338273);
         int y = 76;

         for (StationScreen.Row row : this.rows(before, after)) {
            Gui.text(g, this.font, row.label, px, y, -9412532);
            Gui.text(g, this.font, row.before, 300, y, -13161440);
            Gui.text(g, this.font, row.after, 350, y, row.changed ? -13995976 : -13161440);
            y += 11;
         }

         g.fill(px, y + 2, 394, y + 3, -11445673);
         y += 8;
         var details = net.wtworkshop.station.StationDetails.lines(((StationMenu)this.menu).inputList(),outcome);
         if(details.isEmpty()){
            ToolData data=WorkshopTool.data(shown);
            details=List.of(data.level()>=net.wtworkshop.WorkshopConfig.maxLevel()?Component.translatable("screen.wt_workshop.preview.maximum"):Component.translatable("screen.wt_workshop.preview.xp",data.xp(),net.wtworkshop.tool.ToolLeveling.xpNeeded(data.level())),Component.translatable("screen.wt_workshop.preview.insert"));
         }
         for(var line:details){if(y>202)break;y=Gui.paragraph(g,this.font,line,px,y,194,outcome.success()?-13995976:-9412532,202);y+=3;}
         Gui.text(g,this.font,Component.translatable("screen.wt_workshop.preview.hover"),px,216,-9412532);
      }
   }

   private List<StationScreen.Row> rows(ItemStack before, ItemStack after) {
      ToolStats b = WorkshopTool.stats(before);
      ToolStats a = WorkshopTool.stats(after);
      ToolData bd = WorkshopTool.data(before);
      ToolData ad = WorkshopTool.data(after);
      List<StationScreen.Row> rows = new ArrayList<>();
      rows.add(
         this.row(
            "durability",
            b == null ? "--" : before.getMaxDamage() - before.getDamageValue() + "",
            a == null ? "--" : after.getMaxDamage() - after.getDamageValue() + ""
         )
      );
      rows.add(this.row("mining_speed", b == null ? "--" : Text.number((double)b.speed()), a == null ? "--" : Text.number((double)a.speed())));
      rows.add(this.row("attack", b == null ? "--" : Text.number((double)(b.attack() + 1.0F)), a == null ? "--" : Text.number((double)(a.attack() + 1.0F))));
      rows.add(this.row("upgrades", b == null ? "--" : b.usedUpgrades() + "/" + b.upgradeSlots(), a == null ? "--" : a.usedUpgrades() + "/" + a.upgradeSlots()));
      rows.add(
         this.row("abilities", b == null ? "--" : b.usedAbilities() + "/" + b.abilitySlots(), a == null ? "--" : a.usedAbilities() + "/" + a.abilitySlots())
      );
      rows.add(this.row("level", bd == null ? "--" : Integer.toString(bd.level()), ad == null ? "--" : Integer.toString(ad.level())));
      return rows;
   }

   private StationScreen.Row row(String key, String before, String after) {
      return new StationScreen.Row(Component.translatable("stat.wt_workshop." + key), before, after, !after.equals("--") && !before.equals(after));
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
      super.render(g, mouseX, mouseY, partial);
      this.renderTooltip(g, mouseX, mouseY);
      if(mouseX>=leftPos+184&&mouseX<leftPos+394&&mouseY>=topPos+145&&mouseY<topPos+230){
         var outcome=StationLogic.compute(menu.inputList(),menu.player().level());var details=net.wtworkshop.station.StationDetails.lines(menu.inputList(),outcome);
         if(!details.isEmpty())g.renderComponentTooltip(font,details,mouseX,mouseY);
      }
   }

   private static record Row(Component label, String before, String after, boolean changed) {
   }
}
