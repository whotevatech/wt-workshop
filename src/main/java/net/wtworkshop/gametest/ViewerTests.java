package net.wtworkshop.gametest;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import net.wtworkshop.*;
import net.wtworkshop.compat.ViewerRecipes;
import net.wtworkshop.builder.PartBuilderLogic;
import net.wtworkshop.station.StationLogic;
import net.wtworkshop.smeltery.*;
import net.wtworkshop.tool.*;
@GameTestHolder("wt_workshop") @PrefixGameTestTemplate(false)
public final class ViewerTests {
 @GameTest(template="empty") public static void catalogHasAllCategoriesAndUniqueIds(GameTestHelper h){
  var recipes=ViewerRecipes.build(h.getLevel());var ids=new HashSet<net.minecraft.resources.ResourceLocation>();
  for(var kind:ViewerRecipes.Kind.values())h.assertTrue(recipes.stream().anyMatch(r->r.kind()==kind),"Empty recipe category "+kind);
  for(var r:recipes){h.assertTrue(ids.add(r.id()),"Duplicate recipe ID "+r.id());for(var s:java.util.stream.Stream.concat(java.util.stream.Stream.concat(r.inputs().stream(),r.catalysts().stream()),java.util.stream.Stream.of(r.output())).toList())h.assertTrue(s.molten()!=null?Molten.get(s.molten().molten())!=null&&s.molten().amount()>0:!s.items().isEmpty()&&s.items().stream().allMatch(i->!i.isEmpty()),"Invalid displayed ingredient "+r.id());}
  org.slf4j.LoggerFactory.getLogger("WT Workshop").info("Validated {} workshop viewer recipes",recipes.size());h.succeed();
 }
 @GameTest(template="empty") public static void displayedPartsMatchPartBuilder(GameTestHelper h){
  for(var r:ViewerRecipes.build(h.getLevel()))if(r.kind()==ViewerRecipes.Kind.PARTS){var mat=r.inputs().getFirst().items().getFirst();var pattern=r.catalysts().getFirst().items().getFirst();var result=PartBuilderLogic.compute(pattern,mat,-1,h.getLevel());h.assertTrue(result.success()&&result.patternCost()==0&&result.materialCost()==mat.getCount()&&ItemStack.matches(result.result(),r.output().items().getFirst()),"Displayed part recipe disagrees with builder "+r.id());}h.succeed();
 }
 @GameTest(template="empty") public static void displayedModifiersMatchStation(GameTestHelper h){
  for(var r:ViewerRecipes.build(h.getLevel()))if(r.kind()==ViewerRecipes.Kind.MODIFIERS){var tool=r.inputs().get(0).items().getFirst();var material=r.inputs().get(1).items().getFirst();var result=StationLogic.compute(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,tool,material),h.getLevel());h.assertTrue(result.success()&&result.consume()[4]==material.getCount()&&ItemStack.matches(result.result(),r.output().items().getFirst()),"Displayed modifier disagrees with station "+r.id());}h.succeed();
 }
 @GameTest(template="empty") public static void displayedMeltingMatchesSmeltery(GameTestHelper h){
  for(var r:ViewerRecipes.build(h.getLevel()))if(r.kind()==ViewerRecipes.Kind.MELTING){var actual=SmelteryBlockEntity.melt(r.inputs().getFirst().items().getFirst(),h.getLevel()).orElseThrow();h.assertTrue(actual.molten().id().equals(r.output().molten().molten())&&actual.amount()==r.output().molten().amount(),"Wrong melting quantity "+r.id());}h.succeed();
 }
 @GameTest(template="empty") public static void displayedCastingMatchesSmeltery(GameTestHelper h){
  for(var r:ViewerRecipes.build(h.getLevel()))if(r.kind()==ViewerRecipes.Kind.CASTING){var fluid=r.inputs().getFirst().molten();var cast=!r.catalysts().isEmpty()?r.catalysts().getFirst().items().getFirst():r.inputs().size()>1?r.inputs().get(1).items().getFirst():ItemStack.EMPTY;var plan=SmelteryBlockEntity.castPlan(cast,Molten.get(fluid.molten()));h.assertTrue(plan!=null&&plan.amount()==fluid.amount()&&plan.consumesCast()==(r.inputs().size()>1)&&ItemStack.matches(plan.result(),r.output().items().getFirst()),"Wrong casting recipe "+r.id());}h.succeed();
 }
 @GameTest(template="empty") public static void modifierAppearanceSurvivesCodecAndPartialProgress(GameTestHelper h){
  for(var type:ToolType.values())for(int flags=0;flags<8;flags++){var data=ToolData.of("iron","wood","string");if((flags&1)!=0)data=data.withModifierAdded(Modifier.HASTE);if((flags&2)!=0)data=data.withModifierAdded(Modifier.LUCK);if((flags&4)!=0)data=data.withModifierAdded(Modifier.DIAMOND);var stack=WorkshopTool.create(type,data);var before=stack.copy();h.assertTrue(ModifierAppearance.flags(stack)==flags&&ItemStack.matches(stack,before),"Appearance mutated tool or selected wrong combination");var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);var encoded=ItemStack.CODEC.encodeStart(ops,stack).getOrThrow();var decoded=ItemStack.CODEC.parse(ops,encoded).getOrThrow();h.assertTrue(ModifierAppearance.flags(decoded)==flags,"Appearance changed after item save/load");}
  var partial=WorkshopTool.create(ToolType.PICKAXE,ToolData.of("iron","wood","string").withProgress(Modifier.HASTE,15).withProgress(Modifier.LUCK,31));h.assertTrue(ModifierAppearance.flags(partial)==0,"Unfinished modifier received completed appearance");h.succeed();
 }
}
