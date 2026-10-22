package net.wtworkshop;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent.Loading;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.wtworkshop.smeltery.SmelteryBlockEntity;
import net.wtworkshop.tool.ToolStats;

@Mod("wt_workshop")
public final class Workshop {
   public static final String ID = "wt_workshop";

   public Workshop(IEventBus bus, ModContainer container) {
      Registration.register(bus);
      bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e)->net.wtworkshop.api.AddonModifiers.freeze());
      container.registerConfig(Type.SERVER, WorkshopConfig.SPEC);
      bus.addListener(Loading.class, e -> ToolStats.clearCache());
      bus.addListener(Reloading.class, e -> ToolStats.clearCache());
      bus.addListener(RegisterCapabilitiesEvent.class, Workshop::capabilities);
   }

   private static void capabilities(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(ItemHandler.BLOCK, (BlockEntityType)Registration.SMELTERY_BE.get(), SmelteryBlockEntity::itemHandler);
   }
}
