package net.wtworkshop.part;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;

public class PatternItem extends Item {
   public final PartType type;

   public PatternItem(PartType type) {
      super(new Properties().stacksTo(16));
      this.type = type;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
      lines.add(Component.translatable("tooltip.wt_workshop.pattern.cost", new Object[]{this.type.cost}).withStyle(ChatFormatting.GOLD));
      lines.add(Component.translatable("tooltip.wt_workshop.pattern.use").withStyle(ChatFormatting.GRAY));
   }
}
