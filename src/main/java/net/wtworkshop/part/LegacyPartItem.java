package net.wtworkshop.part;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.ToolMaterial;

public class LegacyPartItem extends Item {
   private final PartType type;
   private final String material;

   public LegacyPartItem(PartType type, String material) {
      super(new Properties());
      this.type = type;
      this.material = material;
   }

   public String materialId() {
      return this.material;
   }

   public ItemStack convert(ItemStack old) {
      ToolMaterial m = Materials.get(this.material);
      return m == null ? old : PartItem.create(this.type, m).copyWithCount(old.getCount());
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide
         && entity instanceof Player player
         && slot >= 0
         && slot < player.getInventory().items.size()
         && player.getInventory().items.get(slot) == stack) {
         player.getInventory().items.set(slot, this.convert(stack));
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
      lines.add(Component.translatable("tooltip.wt_workshop.legacy").withStyle(ChatFormatting.YELLOW));
   }
}
