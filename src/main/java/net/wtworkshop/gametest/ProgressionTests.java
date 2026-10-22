package net.wtworkshop.gametest;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.gametest.*;
import net.wtworkshop.*;
import net.wtworkshop.tool.*;
import net.wtworkshop.station.*;
@GameTestHolder("wt_workshop") @PrefixGameTestTemplate(false)
public final class ProgressionTests {
 private static StationLogic.Outcome apply(GameTestHelper h,ItemStack tool,ItemStack ingredient){return StationLogic.compute(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,tool,ingredient),h.getLevel());}
 @GameTest(template="empty") public static void expansionOrderCostAndSlots(GameTestHelper h){
  var tool=WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string"));var start=ToolStats.of(ToolType.PICKAXE,WorkshopTool.data(tool));
  h.assertTrue(!apply(h,tool,SlotExpansion.ASTRAL.item()).success(),"Skipped expansion order");
  for(var tier:SlotExpansion.values()){var result=apply(h,tool,tier.item().copyWithCount(2));h.assertTrue(result.success()&&result.consume()[3]==1&&result.consume()[4]==1,"Wrong expansion cost");tool=result.result();h.assertTrue(!apply(h,tool,tier.item()).success(),"Repeated expansion accepted");}
  var end=ToolStats.of(ToolType.PICKAXE,WorkshopTool.data(tool));h.assertTrue(end.upgradeSlots()==start.upgradeSlots()+3&&end.abilitySlots()==start.abilitySlots()+1,"Wrong expansion bonuses");
  var d=WorkshopTool.data(tool).withModifierAdded(Modifier.HASTE).withProgress(Modifier.LUCK,12).withXp(4,55);h.assertTrue(d.withLastModifierRemoved().expansionTier()==3&&d.withMaterial(net.wtworkshop.material.PartRole.HANDLE,"wood").expansionTier()==3,"Data mutation lost expansion");
  var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);var encoded=ToolData.CODEC.encodeStart(ops,d).getOrThrow();h.assertTrue(ToolData.CODEC.parse(ops,encoded).getOrThrow().equals(d),"Save roundtrip changed data");
  ((net.minecraft.nbt.CompoundTag)encoded).remove("expansions");h.assertTrue(ToolData.CODEC.parse(ops,encoded).getOrThrow().expansions().isEmpty(),"Old save cannot load");
  var buf=io.netty.buffer.Unpooled.buffer();try{ToolData.STREAM_CODEC.encode(buf,d);h.assertTrue(ToolData.STREAM_CODEC.decode(buf).equals(d),"Network roundtrip changed data");}finally{buf.release();}h.succeed();
 }
 @GameTest(template="empty") public static void stationPickupAndShiftClickConsumeOriginal(GameTestHelper h){
  for(boolean shift:new boolean[]{false,true}){var p=h.makeMockPlayer(GameType.SURVIVAL);var menu=new StationMenu(1,p.getInventory());menu.getSlot(4).set(WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string")));menu.getSlot(5).set(SlotExpansion.COPPER.item().copyWithCount(2));
   if(shift)menu.quickMoveStack(p,0);else menu.clicked(0,0,ClickType.PICKUP,p);
   h.assertTrue(menu.getSlot(4).getItem().isEmpty()&&menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(5).getItem().getCount()==1,"Output duplicated original or charged wrong count");
   int count=shift?p.getInventory().items.stream().filter(s->s.getItem() instanceof WorkshopTool).mapToInt(ItemStack::getCount).sum():menu.getCarried().getCount();h.assertTrue(count==1,"Expected exactly one upgraded tool");}h.succeed();
 }
 @GameTest(template="empty") public static void fullInventoryPreservesExpansionInputs(GameTestHelper h){var p=h.makeMockPlayer(GameType.SURVIVAL);for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.DIRT,64));var menu=new StationMenu(1,p.getInventory());menu.getSlot(4).set(WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string")));menu.getSlot(5).set(SlotExpansion.COPPER.item());h.assertTrue(menu.quickMoveStack(p,0).isEmpty()&&!menu.getSlot(4).getItem().isEmpty()&&!menu.getSlot(5).getItem().isEmpty(),"Failed transfer consumed expansion");h.succeed();}
 @GameTest(template="empty") public static void stationModifierRepairRemovalConsumeOriginal(GameTestHelper h){
  var base=WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string").withExpansion("copper"));var modified=apply(h,base,new ItemStack(Items.REDSTONE,50));h.assertTrue(modified.success()&&modified.consume()[3]==1,"Modifier leaves original");var removal=apply(h,modified.result(),new ItemStack(Items.SPONGE));h.assertTrue(removal.success()&&removal.consume()[3]==1&&WorkshopTool.data(removal.result()).expansionTier()==1,"Removal lost expansion or duplicates");
  base.setDamageValue(30);var repair=apply(h,base,new ItemStack(Items.IRON_INGOT));h.assertTrue(repair.success()&&repair.consume()[3]==1,"Repair duplicates original");h.succeed();
 }
 private static net.minecraft.server.level.ServerPlayer attacker(GameTestHelper h){var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.setHealth(10);p.setItemInHand(InteractionHand.MAIN_HAND,WorkshopTool.create(ToolType.SWORD,ToolData.of("iron","wood","string").withModifierAdded(Modifier.SIPHONING)));return p;}
 @GameTest(template="empty") public static void siphoningUsesHealthDamageAndCooldown(GameTestHelper h){
  var p=attacker(h);var mob=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,1));mob.setNoAi(true);float before=mob.getHealth();mob.hurt(p.damageSources().playerAttack(p),4);float lost=before-mob.getHealth();h.assertTrue(lost>0&&Math.abs(p.getHealth()-(10+lost*.05f))<.001,"Siphoning did not follow real health loss: "+p.getHealth()+" / "+lost);
  float healed=p.getHealth();var mob2=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,1));mob2.hurt(p.damageSources().playerAttack(p),4);h.assertTrue(p.getHealth()==healed,"Cooldown allowed immediate second heal");h.succeed();
 }
 @GameTest(template="empty") public static void absorptionAndOverkillDoNotInflateHealing(GameTestHelper h){
  var p=attacker(h);var mob=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,1));mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,200,4));h.assertTrue(mob.getAbsorptionAmount()>=20,"Absorption fixture missing shield");mob.hurt(p.damageSources().playerAttack(p),4);h.assertTrue(p.getHealth()==10,"Absorbed damage healed attacker");
  var weak=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,1));weak.setHealth(1);weak.hurt(p.damageSources().playerAttack(p),100);h.assertTrue(Math.abs(p.getHealth()-10.05f)<.001,"Overkill inflated healing");h.succeed();
 }
 @GameTest(template="empty") public static void allVisualFlagsSurviveItemSave(GameTestHelper h){var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);for(var type:ToolType.values()){var d=ToolData.of("iron","wood","string");for(var m:ModifierAppearance.STYLES)d=d.withModifierAdded(m);var item=WorkshopTool.create(type,d);var restored=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,item).getOrThrow()).getOrThrow();h.assertTrue(ModifierAppearance.flags(restored)==127,"Lost new appearance flags");}h.succeed();}
}
