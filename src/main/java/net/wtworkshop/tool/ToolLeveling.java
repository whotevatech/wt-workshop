package net.wtworkshop.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.wtworkshop.Registration;
import net.wtworkshop.WorkshopConfig;
import net.wtworkshop.material.Trait;

public final class ToolLeveling {
   private ToolLeveling() {
   }

   public record Gains(int upgrades,int abilities,double statPercent){}
   public static Gains gains(ToolType type,ToolData before,ToolData after){var b=ToolStats.of(type,before);var a=ToolStats.of(type,after);return new Gains(a.upgradeSlots()-b.upgradeSlots(),a.abilitySlots()-b.abilitySlots(),(after.level()-before.level())*WorkshopConfig.levelBonus()*100);}
   public static long xpNeeded(int level) {
      return (long)WorkshopConfig.xpBase() + (long)WorkshopConfig.xpGrowth() * (long)level;
   }

   public static void addXp(ItemStack stack, Player player, int amount) {
      ToolData d = WorkshopTool.data(stack);
      if (!player.level().isClientSide && d != null && amount > 0) {
         int maxLevel = WorkshopConfig.maxLevel();
         if (d.level() < maxLevel) {
            ToolStats s = ToolStats.of(((WorkshopTool)stack.getItem()).type, d);
            int lustrous = s.trait(Trait.LUSTROUS);
            long xp = d.xp() + (long)amount * (1L << Math.min(lustrous, 3));
            int level = d.level();

            boolean leveled;
            for (leveled = false; level < maxLevel && xp >= xpNeeded(level); leveled = true) {
               xp -= xpNeeded(level);
               level++;
            }

            if (level >= maxLevel) {
               xp = 0L;
            }

            ToolData updated = d.withXp(level, xp);
            if(leveled)WorkshopTool.setDataKeepWear(stack, updated);else stack.set(Registration.TOOL_DATA.get(),updated);
            if(leveled && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)WorkshopMilestones.inspect(serverPlayer,stack);
            if (leveled) {
               var gains = gains(((WorkshopTool)stack.getItem()).type,d,updated);
               player.displayClientMessage(Component.translatable("message.wt_workshop.level_up",stack.getHoverName(),level).withStyle(ChatFormatting.GOLD),true);
               player.displayClientMessage(Component.translatable("message.wt_workshop.level_rewards",stack.getHoverName(),level,gains.upgrades(),gains.abilities(),net.wtworkshop.util.Text.number(gains.statPercent())).withStyle(ChatFormatting.GOLD),false);
               player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
            }
         }
      }
   }
}
