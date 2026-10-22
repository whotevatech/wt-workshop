package net.wtworkshop.station;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.wtworkshop.tool.*;
public final class StationDetails {
 public static List<Component> lines(List<ItemStack> inputs,StationLogic.Outcome outcome){
  var lines=new ArrayList<Component>();if(outcome.message()!=null)lines.add(outcome.message());if(!outcome.success())return List.copyOf(lines);
  for(int i=0;i<inputs.size();i++)if(i!=StationLogic.TOOL&&outcome.consume()[i]>0)lines.add(Component.translatable("screen.wt_workshop.preview.cost",outcome.consume()[i],inputs.get(i).getHoverName()));
  var before=inputs.get(StationLogic.TOOL);var result=outcome.result();var b=WorkshopTool.data(before);var a=WorkshopTool.data(result);
  if(b!=null&&a!=null){var bs=WorkshopTool.stats(before);var as=WorkshopTool.stats(result);int upgrades=as.usedUpgrades()-bs.usedUpgrades(),abilities=as.usedAbilities()-bs.usedAbilities();if(upgrades>0||abilities>0)lines.add(Component.translatable("screen.wt_workshop.preview.uses",Math.max(0,upgrades),Math.max(0,abilities)));if(as.upgradeSlots()>bs.upgradeSlots()||as.abilitySlots()>bs.abilitySlots())lines.add(Component.translatable("screen.wt_workshop.preview.adds",as.upgradeSlots()-bs.upgradeSlots(),as.abilitySlots()-bs.abilitySlots()));
   for(var m:Modifier.values())if(a.modifierLevel(m)>b.modifierLevel(m)||a.progressOf(m)!=b.progressOf(m)){if(a.modifierLevel(m)>=b.modifierLevel(m))lines.add(m.description());}
   for(var m:net.wtworkshop.api.AddonModifiers.values())if(a.modifierLevel(m.id())>b.modifierLevel(m.id())||a.progressOf(m.id())!=b.progressOf(m.id()))lines.add(m.description().get());
  }return List.copyOf(lines);
 }
}
