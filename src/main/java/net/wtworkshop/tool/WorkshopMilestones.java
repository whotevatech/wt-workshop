package net.wtworkshop.tool;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.wtworkshop.station.StationLogic;
@EventBusSubscriber(modid="wt_workshop")
public final class WorkshopMilestones {
 public static void award(ServerPlayer p,String id){var a=p.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("wt_workshop","workshop/"+id));if(a!=null&&!p.getAdvancements().getOrStartProgress(a).isDone())p.getAdvancements().award(a,"done");}
 public static void inspect(ServerPlayer p,ItemStack stack){if(!(stack.getItem() instanceof WorkshopTool tool))return;var data=WorkshopTool.data(stack);if(data==null||!data.valid())return;var type=tool.type;award(p,"assembled");if(type==ToolType.PICKAXE)award(p,"pickaxe");if(type.isHeavy())award(p,"heavy_tool");if(data.level()>=1)award(p,"level_1");if(data.level()>=5)award(p,"level_5");if(data.level()>=10)award(p,"level_10");if(data.modifierLevel(Modifier.HASTE)>0)award(p,"haste");if(data.expansionTier()>0)award(p,"expansion");}
 public static void station(ServerPlayer p,ItemStack before,ItemStack result){inspect(p,result);if(WorkshopTool.data(before)!=null&&before.getDamageValue()>result.getDamageValue()&&before.getMaxDamage()==result.getMaxDamage()&&WorkshopTool.data(before).equals(WorkshopTool.data(result)))award(p,"repair");}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%20==0)for(int i=0;i<p.getInventory().getContainerSize();i++)inspect(p,p.getInventory().getItem(i));}
}
