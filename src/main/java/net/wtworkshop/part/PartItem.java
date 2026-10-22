package net.wtworkshop.part;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.ItemLike;
import net.wtworkshop.Registration;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.material.Trait;
import net.wtworkshop.util.Text;
import org.jetbrains.annotations.Nullable;

public class PartItem extends Item {
   public final PartType type;

   public PartItem(PartType type) {
      super(new Properties());
      this.type = type;
   }

   public static ItemStack create(PartType type, ToolMaterial material) {
      ItemStack stack = new ItemStack((ItemLike)Registration.PARTS.get(type).get());
      stack.set((DataComponentType)Registration.PART_MATERIAL.get(), material.id());
      return stack;
   }

   @Nullable
   public static ToolMaterial material(ItemStack stack) {
      String id = (String)stack.get((DataComponentType)Registration.PART_MATERIAL.get());
      return id == null ? null : Materials.get(id);
   }

   public Component getName(ItemStack stack) {
      ToolMaterial m = material(stack);
      return (Component)(m == null
         ? super.getName(stack)
         : Component.translatable("item.wt_workshop.part_with_material", new Object[]{m.displayName(), super.getName(stack)}));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
      ToolMaterial m = material(stack);
      if (m != null && m.supports(this.type.role)) {
         statLines(m, this.type, lines);
         Map<Trait, Integer> traits = m.traits(this.type.role);
         boolean details = Text.shiftDown();
         traits.forEach((t, l) -> {
            lines.add(t.displayName(l));
            if (details) {
               lines.add(Text.indent(t.description()));
            }
         });
         if (!traits.isEmpty() && !details) {
            lines.add(Text.holdShift());
         }
      } else {
         lines.add(Component.translatable("tooltip.wt_workshop.part.invalid").withStyle(ChatFormatting.RED));
      }
   }

   public static void statLines(ToolMaterial m, PartType type, List<Component> lines) {
      switch (type.role) {
         case HEAD: {
            ToolMaterial.HeadStats h = m.head();
            lines.add(Text.stat("durability", Integer.toString(Materials.headDurability(m))));
            lines.add(Text.stat("mining_speed", Text.number((double)Materials.headSpeed(m))));
            lines.add(Text.stat("attack", Text.number((double)h.attack())));
            lines.add(Text.stat("tier", tierName(h.tier())));
            break;
         }
         case HANDLE: {
            ToolMaterial.HandleStats h = m.handle();
            lines.add(Text.stat("durability", Text.multiplier(h.durability())));
            lines.add(Text.stat("mining_speed", Text.multiplier(h.speed())));
            lines.add(Text.stat("attack", Text.multiplier(h.attack())));
            break;
         }
         case BINDING:
            ToolMaterial.BindingStats b = m.binding();
            lines.add(Text.stat("durability", Text.percent(b.durability())));
            lines.add(Text.stat("mining_speed", Text.percent(b.speed())));
      }
   }

   public static Component tierName(Tier tier) {
      String key = tier instanceof Tiers t ? t.name().toLowerCase(Locale.ROOT) : "custom";
      return Component.translatable("wt_workshop.tier." + key);
   }
}
