package net.wtworkshop.gametest;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.wtworkshop.tool.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Development-only screenshot check, excluded from the release JAR. */
@EventBusSubscriber(modid="wt_workshop",value=Dist.CLIENT)
public final class PreviewCapture {
 private static int frames; private static boolean opened; private static int phase;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!System.getProperty("wt_workshop.previewMode","station").equals("station"))return;
  String target=System.getProperty("wt_workshop.capture","");if(target.isEmpty())return;
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
  if(!opened){mc.setScreen(new PartsPreview());opened=true;return;}
  if(++frames==25){
   try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(target.replace(".png","-"+phase+".png")));}catch(Exception ex){throw new RuntimeException(ex);}
   frames=0;phase++;
   if(phase==1){
    var menu=new net.wtworkshop.station.StationMenu(0,mc.player.getInventory());
    var tool=WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string"));
    menu.getSlot(4).set(tool);menu.getSlot(5).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.REDSTONE,64));
    menu.getSlot(0).set(net.wtworkshop.station.StationLogic.compute(menu.inputList(),mc.level).result());
    mc.setScreen(new net.wtworkshop.client.StationScreen(menu,mc.player.getInventory(),Component.literal("Tool Station")));
   }else if(phase==2){
    var menu=new net.wtworkshop.builder.PartBuilderMenu(0,mc.player.getInventory());menu.getSlot(1).set(new net.minecraft.world.item.ItemStack(net.wtworkshop.Registration.BLANK_PATTERN.get(),8));
    mc.setScreen(new net.wtworkshop.client.BuilderScreen(menu,mc.player.getInventory(),Component.literal("Part Builder")));
   }else if(phase==3){
    var menu=new net.wtworkshop.smeltery.SmelteryMenu(0,mc.player.getInventory());menu.getSlot(0).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT,8));menu.getSlot(3).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL,8));
    mc.setScreen(new net.wtworkshop.client.SmelteryScreen(menu,mc.player.getInventory(),Component.literal("Smeltery")));
   }else if(phase==4){mc.setScreen(new ToolPreview());}
   else if(phase==5){
    mc.setScreen(null);mc.options.hideGui=true;
    mc.getSingleplayerServer().execute(()->{
     var level=mc.getSingleplayerServer().overworld();level.setDayTime(6000);
     for(int x=-1;x<=10;x++)for(int z=-1;z<=7;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,-60,z),net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState());
     level.setBlockAndUpdate(new net.minecraft.core.BlockPos(1,-59,2),net.wtworkshop.Registration.TOOL_STATION.get().defaultBlockState());
     level.setBlockAndUpdate(new net.minecraft.core.BlockPos(4,-59,2),net.wtworkshop.Registration.PART_BUILDER.get().defaultBlockState());
     level.setBlockAndUpdate(new net.minecraft.core.BlockPos(7,-59,2),net.wtworkshop.Registration.SMELTERY.get().defaultBlockState());
     var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(9,-57,10,145,20);
    });
   }else mc.stop();
  }
 }
 private static final class PartsPreview extends Screen {
  PartsPreview(){super(Component.literal("Workbench and part graphics"));}
  @Override public void render(GuiGraphics g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xff332b23);g.drawCenteredString(font,"WT WORKSHOP / WORKBENCHES AND PARTS",width/2,20,0xffead5aa);
   var r=net.wtworkshop.Registration.class;
   var blocks=java.util.List.of(new net.minecraft.world.item.ItemStack(net.wtworkshop.Registration.TOOL_STATION_ITEM.get()),new net.minecraft.world.item.ItemStack(net.wtworkshop.Registration.PART_BUILDER_ITEM.get()),new net.minecraft.world.item.ItemStack(net.wtworkshop.Registration.SMELTERY_ITEM.get()),WorkshopTool.create(ToolType.AXE,ToolData.of("iron","wood","string")));
   String[] labels={"TOOL STATION","PART BUILDER","SMELTERY","AXE"};
   for(int i=0;i<4;i++){int x=width/2-290+i*150;g.pose().pushPose();g.pose().translate(x,52,0);g.pose().scale(6,6,1);g.renderItem(blocks.get(i),0,0);g.pose().popPose();g.drawCenteredString(font,labels[i],x+48,160,0xffead5aa);}
   var types=java.util.List.of(net.wtworkshop.part.PartType.AXE_HEAD,net.wtworkshop.part.PartType.SHOVEL_HEAD,net.wtworkshop.part.PartType.SWORD_BLADE,net.wtworkshop.part.PartType.GUARD,net.wtworkshop.part.PartType.HANDLE,net.wtworkshop.part.PartType.TOUGH_HANDLE,net.wtworkshop.part.PartType.BINDING,net.wtworkshop.part.PartType.TOUGH_BINDING);
   for(int i=0;i<types.size();i++){var type=types.get(i);var material=type.role==net.wtworkshop.material.PartRole.HANDLE?net.wtworkshop.material.Materials.WOOD:type.role==net.wtworkshop.material.PartRole.BINDING&&type!=net.wtworkshop.part.PartType.GUARD?net.wtworkshop.material.Materials.STRING:net.wtworkshop.material.Materials.IRON;
    int x=width/2-270+i%4*140,y=200+i/4*100;var stack=net.wtworkshop.part.PartItem.create(type,material);
    g.pose().pushPose();g.pose().translate(x+20,y,0);g.pose().scale(3,3,1);g.renderItem(stack,0,0);g.pose().popPose();g.drawCenteredString(font,type.name().replace('_',' '),x+44,y+54,0xffead5aa);g.renderItem(stack,x+36,y+70);
   }
  }
 }
 private static final class ToolPreview extends Screen {
  ToolPreview(){super(Component.literal("WT Workshop tools"));}
  @Override public void render(GuiGraphics g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xff20282e);g.drawCenteredString(font,"WT WORKSHOP / TOOL GRAPHICS CHECK",width/2,22,0xffe8bc70);
   for(var t:ToolType.values()){
    int col=t.ordinal()%4,row=t.ordinal()/4,x=width/2-270+col*140,y=66+row*180;
    var stack=WorkshopTool.create(t,ToolData.of("iron","wood","string"));
    g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(7,7,1);g.renderItem(stack,0,0);g.pose().popPose();
    g.drawCenteredString(font,t.name().replace('_',' '),x+56,y+120,0xffeeeeee);g.renderItem(stack,x+48,y+136);
   }
  }
 }
}
