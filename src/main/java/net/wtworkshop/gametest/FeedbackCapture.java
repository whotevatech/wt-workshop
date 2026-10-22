package net.wtworkshop.gametest;
import java.nio.file.Path;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.wtworkshop.tool.*;
import net.wtworkshop.station.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="wt_workshop",value=Dist.CLIENT)
public final class FeedbackCapture {
 private static int phase,frames;private static boolean opened;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(!System.getProperty("wt_workshop.previewMode","").equals("feedback"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;var target=System.getProperty("wt_workshop.capture","");if(target.isEmpty())return;if(!opened){open();opened=true;return;}if(++frames==35){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(target.replace(".png","-"+phase+".png")));}catch(Exception ex){throw new RuntimeException(ex);}frames=0;if(++phase<6)open();else mc.stop();}}
 private static void open(){var mc=Minecraft.getInstance();mc.options.hideGui=false;
 if(phase==5){mc.setScreen(null);mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var tool=WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string").withXp(4,ToolLeveling.xpNeeded(4)-1));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,tool);ToolLeveling.addXp(tool,p,1);});return;}
 var menu=new StationMenu(0,mc.player.getInventory());var data=ToolData.of("iron","wood","string");
 if(phase==2)for(int i=0;i<5;i++)data=data.withModifierAdded(Modifier.HASTE);
 var tool=WorkshopTool.create(ToolType.PICKAXE,data);if(phase==3)tool.setDamageValue(100);
 menu.getSlot(4).set(tool);menu.getSlot(5).set(phase==3?new ItemStack(Items.IRON_INGOT,2):phase==4?SlotExpansion.COPPER.item():new ItemStack(Items.REDSTONE,phase==1?3:64));
 menu.getSlot(0).set(StationLogic.compute(menu.inputList(),mc.level).result());mc.setScreen(new net.wtworkshop.client.StationScreen(menu,mc.player.getInventory(),Component.literal("Tool Station")));
 }
}
