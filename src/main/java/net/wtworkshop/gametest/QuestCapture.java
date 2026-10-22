package net.wtworkshop.gametest;
import java.nio.file.Path;
import net.minecraft.client.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="wt_workshop",value=Dist.CLIENT)
public final class QuestCapture {
 private static int frames,phase;private static boolean opened;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(!System.getProperty("wt_workshop.previewMode","").equals("quests"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;var target=System.getProperty("wt_workshop.capture","");if(target.isEmpty())return;
 try{var api=Class.forName("dev.ftb.mods.ftbquests.client.FTBQuestsClient");if(!((Boolean)api.getMethod("isClientDataLoaded").invoke(null)))return;var file=api.getMethod("getClientQuestFile").invoke(null);
 if(!opened){for(String id:new String[]{"6E80AEE1B72E9A54","624923810948DA1B","6C4A15CFD71FD523","6C1E4EA823CAB4D6","66AB120F7AF9D402","665D74C35742CE3D","60635C65D8377FB2","673C94712930649A","67CDAADC44543552","6066FE5A361360F1","6AE0C05E91B6648D","604E278D75C2C5A6","6C14C474D72042AF"})if(file.getClass().getMethod("getQuest",long.class).invoke(file,Long.parseUnsignedLong(id,16))==null)throw new IllegalStateException("Missing FTB quest "+id);org.slf4j.LoggerFactory.getLogger("WT Workshop").info("Verified 13 FTB Workshop quests in live client");open("64DF4AD2A7F23AD4");opened=true;return;}
 if(++frames==50){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(target.replace(".png","-"+phase+".png")));}frames=0;if(++phase==1)open("6E80AEE1B72E9A54");else if(phase==2)open("66AB120F7AF9D402");else mc.stop();}
 }catch(Exception ex){throw new RuntimeException(ex);}}
 private static void open(String id)throws Exception{Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile").getMethod("openBookToQuestObject",long.class).invoke(null,Long.parseUnsignedLong(id,16));}
}
