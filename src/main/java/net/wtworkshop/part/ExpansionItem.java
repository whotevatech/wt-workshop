package net.wtworkshop.part;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.wtworkshop.WorkshopConfig;
import net.wtworkshop.tool.SlotExpansion;
public final class ExpansionItem extends Item {
 public final SlotExpansion expansion;
 public ExpansionItem(SlotExpansion expansion){super(new Properties().stacksTo(16));this.expansion=expansion;}
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
  lines.add(Component.translatable("tooltip.wt_workshop.expansion",expansion.tier(),WorkshopConfig.expansionUpgradeSlots()).withStyle(ChatFormatting.GOLD));
  if(expansion==SlotExpansion.ASTRAL&&WorkshopConfig.finalExpansionAbilitySlots()>0)lines.add(Component.translatable("tooltip.wt_workshop.expansion_ability",WorkshopConfig.finalExpansionAbilitySlots()).withStyle(ChatFormatting.LIGHT_PURPLE));
  lines.add(Component.translatable("tooltip.wt_workshop.expansion_order").withStyle(ChatFormatting.GRAY));
  lines.add(Component.translatable("tooltip.wt_workshop.expansion_permanent").withStyle(ChatFormatting.DARK_GRAY));
 }
}
