package net.wtworkshop.gametest;
import java.nio.file.Path;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.wtworkshop.compat.*;
import net.wtworkshop.tool.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="wt_workshop",value=Dist.CLIENT)
public final class ViewerCapture {
 private static int ticks,phase=-1,frames;
 private static final int CATEGORIES=ViewerRecipes.Kind.values().length;
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  if(!System.getProperty("wt_workshop.previewMode","").equals("viewer"))return;
  String target=System.getProperty("wt_workshop.capture","");if(target.isEmpty())return;
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
  boolean emi=net.neoforged.fml.ModList.get().isLoaded("emi"),jei=net.neoforged.fml.ModList.get().isLoaded("jei");
  if(phase<0){
   if(++ticks>1600)throw new IllegalStateException("Viewer did not initialize");
   if(emi){if(!ViewerCaptureEmi.ready())return;}
   else if(jei){if(!ViewerCaptureJei.ready())return;}
   else{phase=CATEGORIES;mc.options.hideGui=false;mc.setScreen(new ModifierPreview());return;}
   phase=0;mc.options.hideGui=false;mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));open(emi);return;
  }
  if(++frames==30){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(target.replace(".png","-"+phase+".png")));}catch(Exception e){throw new RuntimeException(e);}frames=0;phase++;if(phase<CATEGORIES)open(emi);else if(phase==CATEGORIES)mc.setScreen(new ModifierPreview());else if(phase==CATEGORIES+1)mc.setScreen(new ModifierPreview(new ToolType[]{ToolType.SHOVEL,ToolType.HOE,ToolType.SWORD}));else if(phase==CATEGORIES+2)mc.setScreen(new ModifierPreview(new ToolType[]{ToolType.EXCAVATOR,ToolType.BROAD_AXE,ToolType.PICKAXE}));else mc.stop();}
 }
 private static void open(boolean emi){var kind=ViewerRecipes.Kind.values()[phase];if(emi)ViewerCaptureEmi.open(kind);else ViewerCaptureJei.open(kind);}
 private static final class ModifierPreview extends Screen {
  private final ToolType[] rows;
  ModifierPreview(){this(new ToolType[]{ToolType.PICKAXE,ToolType.AXE,ToolType.HAMMER});}
  ModifierPreview(ToolType[] rows){super(Component.literal("Modifier appearance"));this.rows=rows;}
  @Override public void render(GuiGraphics g,int mx,int my,float delta){
   g.fill(0,0,width,height,0xff20282e);g.drawCenteredString(font,"WT WORKSHOP / VISIBLE MODIFIERS",width/2,18,0xffead5aa);
   String[] labels={"QUARTZ","REINFORCED","FIERY","SIPHONING","ALL SEVEN"};int[] flags={8,16,32,64,127};
   for(int i=0;i<5;i++){int x=width/2-295+i*120;g.drawCenteredString(font,labels[i],x+42,50,0xffead5aa);}
   for(int row=0;row<3;row++)for(int col=0;col<5;col++){var data=ToolData.of("iron","wood","string");for(int bit=0;bit<ModifierAppearance.STYLES.size();bit++)if((flags[col]&(1<<bit))!=0)data=data.withModifierAdded(ModifierAppearance.STYLES.get(bit));var stack=WorkshopTool.create(rows[row],data);int x=width/2-295+col*120,y=75+row*120;g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(5,5,1);g.renderItem(stack,0,0);g.pose().popPose();g.renderItem(stack,x+32,y+86);}
  }
 }
}
