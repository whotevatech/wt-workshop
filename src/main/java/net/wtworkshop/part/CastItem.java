package net.wtworkshop.part;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import org.jetbrains.annotations.Nullable;

public class CastItem extends Item {
   @Nullable
   public final PartType type;

   public CastItem(@Nullable PartType type) {
      super(new Properties().stacksTo(16));
      this.type = type;
   }

   public int amount() {
      return this.type == null ? 90 : this.type.castAmount();
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
      lines.add(Component.translatable("tooltip.wt_workshop.cast.cost", new Object[]{this.amount() / 90}).withStyle(ChatFormatting.GOLD));
      lines.add(Component.translatable("tooltip.wt_workshop.cast.use").withStyle(ChatFormatting.GRAY));
   }
}
