package net.wtworkshop.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.wtworkshop.*;
import net.wtworkshop.material.*;
import net.wtworkshop.part.*;
import net.wtworkshop.smeltery.*;
import net.wtworkshop.station.*;
import net.wtworkshop.tool.*;

@GameTestHolder(Workshop.ID) @PrefixGameTestTemplate(false)
public final class ReviewTests {
    private static SmelteryBlockEntity smeltery(GameTestHelper h) {
        var be=new SmelteryBlockEntity(h.absolutePos(BlockPos.ZERO), Registration.SMELTERY.get().defaultBlockState());
        be.setLevel(h.getLevel());return be;
    }
    private static void tick(GameTestHelper h,SmelteryBlockEntity be,int n){for(int i=0;i<n;i++)SmelteryBlockEntity.serverTick(h.getLevel(),be.getBlockPos(),be.getBlockState(),be);}
    private static CompoundTag saved(GameTestHelper h,SmelteryBlockEntity be){return be.saveWithoutMetadata(h.getLevel().registryAccess());}
    @GameTest(template="empty") public static void inputChangeResetsMelt(GameTestHelper h){
        var be=smeltery(h);be.items().setStackInSlot(0,new ItemStack(Items.IRON_INGOT));be.items().setStackInSlot(3,new ItemStack(Items.COAL));tick(h,be,20);
        h.assertTrue(saved(h,be).getIntArray("melt")[0]>0,"Iron did not start melting");
        be.items().setStackInSlot(0,new ItemStack(Items.GOLD_INGOT));
        h.assertTrue(saved(h,be).getIntArray("melt")[0]==0,"Replacement inherited melt progress");h.succeed();
    }
    @GameTest(template="empty") public static void meltingPersistsAndConsumesExactlyOne(GameTestHelper h){
        var be=smeltery(h);be.items().setStackInSlot(0,new ItemStack(Items.IRON_INGOT,2));be.items().setStackInSlot(3,new ItemStack(Items.COAL));
        int time=SmelteryBlockEntity.melt(be.items().getStackInSlot(0),h.getLevel()).orElseThrow().time();tick(h,be,time);
        h.assertTrue(be.stored()==90&&be.items().getStackInSlot(0).getCount()==1,"Melting cost or volume wrong");
        var copy=smeltery(h);copy.loadWithComponents(saved(h,be),h.getLevel().registryAccess());
        h.assertTrue(copy.stored()==90&&copy.items().getStackInSlot(0).getCount()==1,"Reload lost smeltery contents");h.succeed();
    }
    @GameTest(template="empty") public static void castChangeResetsProgress(GameTestHelper h){
        var be=smeltery(h);var tag=new CompoundTag();var tank=new net.minecraft.nbt.ListTag();var f=new CompoundTag();f.putString("molten","iron");f.putInt("amount",810);tank.add(f);tag.put("tank",tank);
        tag.put("items",be.items().serializeNBT(h.getLevel().registryAccess()));be.loadWithComponents(tag,h.getLevel().registryAccess());
        be.items().setStackInSlot(4,new ItemStack(Registration.INGOT_CAST.get()));tick(h,be,39);
        h.assertTrue(be.items().getStackInSlot(5).isEmpty(),"Cast finished early");
        be.items().setStackInSlot(4,new ItemStack(Registration.CASTS.get(PartType.PICKAXE_HEAD).get()));tick(h,be,1);
        h.assertTrue(be.items().getStackInSlot(5).isEmpty()&&be.stored()==810,"New cast inherited progress");tick(h,be,39);
        h.assertTrue(be.items().getStackInSlot(5).getItem() instanceof PartItem&&be.stored()==540,"Cast failed exact cost or timing");
        h.assertTrue(!be.items().getStackInSlot(4).isEmpty(),"Reusable cast consumed");h.succeed();
    }
    @GameTest(template="empty") public static void allToolTypesAssemble(GameTestHelper h){
        for(var type:ToolType.values()){
            var result=StationLogic.compute(List.of(PartItem.create(type.head,Materials.IRON),PartItem.create(type.handle,Materials.WOOD),PartItem.create(type.binding,Materials.STRING),ItemStack.EMPTY,ItemStack.EMPTY),h.getLevel());
            h.assertTrue(result.success()&&result.result().is(Registration.TOOLS.get(type).get()),"Assembly failed for "+type);
            h.assertTrue(WorkshopTool.usable(result.result()),"Assembled unusable tool "+type);
        }h.succeed();
    }
    @GameTest(template="empty") public static void fullInventoryPreservesAssemblyInputs(GameTestHelper h){
        var p=h.makeMockPlayer(GameType.SURVIVAL);for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.DIRT,64));
        var menu=new StationMenu(1,p.getInventory());menu.getSlot(1).set(PartItem.create(PartType.PICKAXE_HEAD,Materials.IRON));menu.getSlot(2).set(PartItem.create(PartType.HANDLE,Materials.WOOD));menu.getSlot(3).set(PartItem.create(PartType.BINDING,Materials.STRING));
        h.assertTrue(menu.quickMoveStack(p,0).isEmpty(),"Full inventory accepted output");h.assertTrue(!menu.getSlot(1).getItem().isEmpty()&&!menu.getSlot(2).getItem().isEmpty()&&!menu.getSlot(3).getItem().isEmpty(),"Failed transfer consumed parts");h.succeed();
    }
    @GameTest(template="empty") public static void legacyMigrationKeepsProgressAndName(GameTestHelper h){
        var stack=new ItemStack(Registration.TOOLS.get(ToolType.PICKAXE).get());var tag=new CompoundTag();tag.putBoolean("assembled",true);tag.putString("head","iron");tag.putString("handle","wood");tag.putString("binding","string");tag.putInt("redstone",2);tag.putInt("level",4);tag.putLong("xp",55);tag.putString("other_mod","keep");
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));stack.set(DataComponents.MAX_DAMAGE,500);stack.set(DataComponents.DAMAGE,250);stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Old Friend"));
        ((WorkshopTool)stack.getItem()).migrateLegacy(stack);var d=WorkshopTool.data(stack);
        h.assertTrue(d!=null&&d.level()==4&&d.xp()==55&&d.modifierLevel(Modifier.HASTE)==2,"Legacy progress changed");
        h.assertTrue(stack.getHoverName().getString().equals("Old Friend")&&stack.get(DataComponents.CUSTOM_DATA).copyTag().getString("other_mod").equals("keep"),"Migration lost custom data");
        h.assertTrue(Math.abs(stack.getDamageValue()/(double)stack.getMaxDamage()-.5)<.01,"Wear ratio changed");h.succeed();
    }
    @GameTest(template="empty") public static void successfulHeavyBreakStillMinesArea(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,WorkshopTool.create(ToolType.HAMMER,ToolData.of("iron","wood","string")));
        var pos=h.absolutePos(new BlockPos(1,1,1));p.setPos(pos.getX()+.5,pos.getY()+4,pos.getZ()+.5);p.setXRot(90);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.getLevel().setBlockAndUpdate(pos.offset(x,0,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        h.assertTrue(p.gameMode.destroyBlock(pos),"Original block did not break");
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.assertTrue(h.getLevel().getBlockState(pos.offset(x,0,z)).isAir(),"Area mining missed a block");h.succeed();
    }
    @GameTest(template="empty") public static void canceledHeavyBreakLeavesNeighbors(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,WorkshopTool.create(ToolType.HAMMER,ToolData.of("iron","wood","string")));
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.getLevel().setBlockAndUpdate(pos.offset(x,0,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        java.util.function.Consumer<BlockEvent.BreakEvent> cancel=e->{if(e.getPlayer()==p)e.setCanceled(true);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,cancel);
        try {p.gameMode.destroyBlock(pos);} finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel);}
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.assertTrue(h.getLevel().getBlockState(pos.offset(x,0,z)).is(net.minecraft.world.level.block.Blocks.STONE),"Canceled heavy break removed a block");h.succeed();
    }
}
