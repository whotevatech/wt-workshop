package net.wtworkshop.compat;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.wtworkshop.client.Gui;
import net.wtworkshop.smeltery.*;
public final class ViewerDrawing {
 public static final int WIDTH=180,HEIGHT=180;
 public static List<Component> tooltip(MoltenStack stack){var m=Molten.get(stack.molten());return List.of(m.displayName(),Component.translatable("viewer.wt_workshop.molten_amount",stack.amount(),String.format(Locale.ROOT,"%.2f",stack.amount()/90.0)));}
 public static void molten(GuiGraphics g,int x,int y,MoltenStack stack){
  var m=Molten.get(stack.molten());if(m==null)return;g.fill(x+2,y+1,x+14,y+15,0xff332b24);g.fill(x+3,y+3,x+13,y+14,m.color());g.fill(x+4,y+3,x+6,y+12,0x70ffffff);g.fill(x+3,y+1,x+13,y+3,0xffb9a88a);var font=Minecraft.getInstance().font;String label=stack.amount()+"u";float scale=Math.min(.65f,16f/font.width(label));g.pose().pushPose();g.pose().translate(x+16-font.width(label)*scale,y+11,200);g.pose().scale(scale,scale,1);g.drawString(font,label,0,0,0xffffffff,true);g.pose().popPose();
 }
 public static int height(ViewerRecipes.Kind kind){return switch(kind){case PATTERNS->80;case PARTS->92;case ASSEMBLY->96;case MELTING,CASTING->116;case ALLOYING->108;case MODIFIERS->180;case EXPANSIONS->144;};}
 public static void background(GuiGraphics g,int height){g.fill(0,0,WIDTH,height,0xffeadfc5);g.fill(0,48,WIDTH,49,0xffa58b66);g.fill(110,13,124,15,0xff62401f);for(int i=0;i<5;i++)g.fill(120+i,9+i,121+i,19-i,0xff62401f);}
 public static void notes(GuiGraphics g,ViewerRecipes.Display recipe){int y=55;for(var note:recipe.notes()){y=Gui.paragraph(g,Minecraft.getInstance().font,note,4,y,WIDTH-8,0xff4d3c2a,height(recipe.kind())-11)+2;}}
}
