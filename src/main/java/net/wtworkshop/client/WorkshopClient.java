package net.wtworkshop.client;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item;
import net.wtworkshop.Registration;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.PartRole;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.part.LegacyPartItem;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.tool.ToolData;
import net.wtworkshop.tool.WorkshopTool;

@EventBusSubscriber(
   modid = "wt_workshop",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public final class WorkshopClient {
   private WorkshopClient() {
   }

   @SubscribeEvent public static void extraModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event){
      for(var type:net.wtworkshop.tool.ToolType.values())for(int i=0;i<net.wtworkshop.tool.ModifierAppearance.STYLES.size();i++)event.register(ModifierLayerModels.layer(type,i));
      for(var type:net.wtworkshop.tool.ToolType.values())for(var m:net.wtworkshop.api.AddonModifiers.values()){var id=m.overlayModel().apply(type);if(id!=null)event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(id));}
   }
   @SubscribeEvent public static void layerModels(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event){
      for(var type:net.wtworkshop.tool.ToolType.values()){
         var id=net.minecraft.client.resources.model.ModelResourceLocation.inventory(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wt_workshop",type.id()));
         var base=event.getModels().get(id);var layers=new net.minecraft.client.resources.model.BakedModel[net.wtworkshop.tool.ModifierAppearance.STYLES.size()];
         for(int i=0;i<layers.length;i++){layers[i]=event.getModels().get(ModifierLayerModels.layer(type,i));if(layers[i]==null)throw new IllegalStateException("Missing modifier layer for "+type+"/"+i);}
         var addons=new java.util.TreeMap<String,net.minecraft.client.resources.model.BakedModel>();
         for(var m:net.wtworkshop.api.AddonModifiers.values()){var model=m.overlayModel().apply(type);if(model!=null){var baked=event.getModels().get(net.minecraft.client.resources.model.ModelResourceLocation.standalone(model));if(baked!=null)addons.put(m.id(),baked);}}
         if(base!=null)event.getModels().put(id,new ModifierLayerModels(base,layers,addons));
      }
   }

   @SubscribeEvent
   public static void screens(RegisterMenuScreensEvent event) {
      event.register((MenuType)Registration.STATION_MENU.get(), StationScreen::new);
      event.register((MenuType)Registration.PART_BUILDER_MENU.get(), BuilderScreen::new);
      event.register((MenuType)Registration.SMELTERY_MENU.get(), SmelteryScreen::new);
   }

   @SubscribeEvent
   public static void colors(Item event) {
      Registration.PARTS.values().forEach(part -> event.register((stack, layer) -> {
            ToolMaterial m = PartItem.material(stack);
            return layer == 0 && m != null ? m.color() : -1;
         }, new ItemLike[]{(ItemLike)part.get()}));
      Registration.LEGACY_PARTS.values().forEach(part -> event.register((stack, layer) -> {
            ToolMaterial m = Materials.get(((LegacyPartItem)part.get()).materialId());
            return m == null ? -1 : m.color();
         }, new ItemLike[]{(ItemLike)part.get()}));
      event.register((stack, layer) -> Materials.ROSE_GOLD.color(), new ItemLike[]{(ItemLike)Registration.ROSE_GOLD_INGOT.get()});
      event.register((stack, layer) -> Materials.AMETHYST_BRONZE.color(), new ItemLike[]{(ItemLike)Registration.AMETHYST_BRONZE_INGOT.get()});
      Registration.TOOLS.values().forEach(tool -> event.register(WorkshopClient::toolColor, new ItemLike[]{(ItemLike)tool.get()}));
   }

   private static int toolColor(ItemStack stack, int layer) {
      ToolData data = WorkshopTool.data(stack);
      if (data != null && layer <= 2) {
         PartRole role = switch (layer) {
            case 0 -> PartRole.HANDLE;
            case 1 -> PartRole.HEAD;
            default -> PartRole.BINDING;
         };
         ToolMaterial m = data.material(role);
         return m == null ? -1 : m.color();
      } else {
         return -1;
      }
   }
}
