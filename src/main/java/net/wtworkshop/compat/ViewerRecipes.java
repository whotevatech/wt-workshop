package net.wtworkshop.compat;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.wtworkshop.*;
import net.wtworkshop.material.*;
import net.wtworkshop.part.*;
import net.wtworkshop.smeltery.*;
import net.wtworkshop.station.StationLogic;
import net.wtworkshop.tool.*;
/** One live, viewer-independent catalogue shared by JEI and EMI. */
public final class ViewerRecipes {
 public enum Kind { PATTERNS,PARTS,ASSEMBLY,MELTING,ALLOYING,CASTING,MODIFIERS,EXPANSIONS;
  public String id(){return name().toLowerCase(Locale.ROOT);}
  public ItemStack icon(){return new ItemStack(switch(this){case PATTERNS,PARTS->Registration.PART_BUILDER_ITEM.get();case ASSEMBLY,MODIFIERS,EXPANSIONS->Registration.TOOL_STATION_ITEM.get();default->Registration.SMELTERY_ITEM.get();});}
  public Component title(){return Component.translatable("category.wt_workshop."+id());}
 }
 public record Slot(List<ItemStack> items,MoltenStack molten){
  public static Slot item(ItemStack s){return new Slot(List.of(s.copy()),null);}
  public static Slot ingredient(Ingredient ingredient,int count){return new Slot(Arrays.stream(ingredient.getItems()).map(s->s.copyWithCount(count)).toList(),null);}
  public static Slot molten(String id,int amount){return new Slot(List.of(),new MoltenStack(id,amount));}
 }
 public record Display(Kind kind,ResourceLocation id,List<Slot> inputs,List<Slot> catalysts,Slot output,List<Component> notes){}
 private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("wt_workshop","/viewer/"+path);}
 private static Component text(String s){return Component.literal(s);}
 private static Component tr(String key,Object... args){return Component.translatable("viewer.wt_workshop."+key,args);}
 private static Slot item(net.minecraft.world.level.ItemLike item){return Slot.item(new ItemStack(item));}
 private static void add(List<Display> out,Kind kind,String id,List<Slot> in,List<Slot> cat,Slot result,Component... notes){out.add(new Display(kind,id(id),in,cat,result,List.of(notes)));}
 public static List<Display> build(Level level){
  if(level==null)return List.of();List<Display> out=new ArrayList<>();
  for(var type:PartType.values()){
   add(out,Kind.PATTERNS,"pattern/"+type.id(),List.of(item(Registration.BLANK_PATTERN.get())),List.of(),item(Registration.PATTERNS.get(type).get()),tr("carve"));
   for(var h:level.getRecipeManager().getAllRecipesFor(Registration.MATERIAL_RECIPE.get())){
    var r=h.value();var m=r.toolMaterial();if(m==null||m.castable()||!m.supports(type.role))continue;
    int cost=r.itemsFor(type.cost);add(out,Kind.PARTS,"part/"+type.id()+"/"+h.id().getNamespace()+"/"+h.id().getPath(),List.of(Slot.ingredient(r.ingredient(),cost)),List.of(item(Registration.PATTERNS.get(type).get())),Slot.item(PartItem.create(type,m)),tr("pattern_kept"),tr("material_cost",cost));
   }
  }
  for(var type:ToolType.values()){
   var head=PartItem.create(type.head,Materials.IRON);var handle=PartItem.create(type.handle,Materials.WOOD);var binding=PartItem.create(type.binding,Materials.STRING);
   var result=StationLogic.compute(List.of(head,handle,binding,ItemStack.EMPTY,ItemStack.EMPTY),level);
   if(result.success())add(out,Kind.ASSEMBLY,"assembly/"+type.id(),List.of(Slot.item(head),Slot.item(handle),Slot.item(binding)),List.of(),Slot.item(result.result()),tr("assembly_example"));
   for(var expansion:SlotExpansion.values())if(expansion.tier()<=WorkshopConfig.expansionTierLimit()){
    var data=ToolData.of("iron","wood","string");for(var previous:SlotExpansion.values())if(previous.tier()<expansion.tier())data=data.withExpansion(previous.id());
    var tool=WorkshopTool.create(type,data);var resultExpansion=StationLogic.compute(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,tool,expansion.item()),level);
    if(resultExpansion.success()){var notes=new ArrayList<Component>();notes.add(tr("expansion_slots",WorkshopConfig.expansionUpgradeSlots()));if(expansion==SlotExpansion.ASTRAL&&WorkshopConfig.finalExpansionAbilitySlots()>0)notes.add(tr("expansion_ability",WorkshopConfig.finalExpansionAbilitySlots()));notes.add(tr("expansion_order",expansion.tier()));notes.add(tr("expansion_permanent"));out.add(new Display(Kind.EXPANSIONS,id("expansion/"+type.id()+"/"+expansion.id()),List.of(Slot.item(tool),Slot.item(expansion.item())),List.of(),Slot.item(resultExpansion.result()),List.copyOf(notes)));}
   }
   for(var mod:net.wtworkshop.api.AddonModifiers.values())if(mod.maxLevel()>0&&mod.allowed().test(type)){
    var ingredients=mod.ingredient().get().getItems();if(ingredients.length==0)continue;
    var input=ingredients[0].copyWithCount(mod.pointsPerLevel());var tool=WorkshopTool.create(type,ToolData.of("iron","wood","string"));
    var upgrade=StationLogic.compute(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,tool,input),level);if(upgrade.success()){
     var alternatives=java.util.Arrays.stream(ingredients).map(s->s.copyWithCount(mod.pointsPerLevel())).toList();
     out.add(new Display(Kind.MODIFIERS,id("addon/"+type.id()+"/"+mod.key().getNamespace()+"/"+mod.key().getPath()),List.of(Slot.item(tool),new Slot(alternatives,null)),List.of(),Slot.item(upgrade.result()),List.of(mod.displayName(1),mod.description().get(),tr("modifier_cost",mod.pointsPerLevel(),mod.slot().name().toLowerCase(Locale.ROOT),mod.maxLevel()),tr("modifier_example"))));
    }
   }
   for(var mod:Modifier.values())if(mod.maximumLevel()>0&&mod.allowedOn(type))for(var ingredient:mod.inputItems()){
    ToolData data=ToolData.of("iron","wood","string");if(mod.requires()!=null)data=data.withModifierAdded(mod.requires());
    var tool=WorkshopTool.create(type,data);int value=mod.valueOf(new ItemStack(ingredient)),cost=(mod.perLevel+value-1)/value;
    var input=new ItemStack(ingredient,cost);var upgrade=StationLogic.compute(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,tool,input),level);if(!upgrade.success())continue;
    var notes=new ArrayList<Component>();notes.add(mod.displayName(1));notes.add(tr("modifier_cost",cost,mod.slot.name().toLowerCase(Locale.ROOT),mod.maximumLevel()));
    if(mod.requires()!=null)notes.add(tr("requires",mod.requires().displayName(1)));
    for(var other:Modifier.values())if(mod.conflictsWith(other))notes.add(tr("conflicts",other.displayName(1)));
    notes.add(tr("modifier_example"));out.add(new Display(Kind.MODIFIERS,id("modifier/"+type.id()+"/"+mod.id()+"/"+net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(ingredient).getPath()),List.of(Slot.item(tool),Slot.item(input)),List.of(),Slot.item(upgrade.result()),List.copyOf(notes)));
   }
  }
  for(var h:level.getRecipeManager().getAllRecipesFor(Registration.MELTING_RECIPE.get())){
   var r=h.value();if(Molten.get(r.molten())!=null)add(out,Kind.MELTING,"melting/"+h.id().getNamespace()+"/"+h.id().getPath(),List.of(Slot.ingredient(r.ingredient(),1)),List.of(),Slot.molten(r.molten(),r.outputAmount()),tr("melting",r.outputAmount(),r.time()/20.0),tr("fuel"));
  }
  for(var m:Materials.all())if(m.castable())for(var type:PartType.values())if(m.supports(type.role)){
   var molten=Molten.forMaterial(m.id());if(molten==null)continue;var part=PartItem.create(type,m);
   add(out,Kind.MELTING,"part_melting/"+m.id()+"/"+type.id(),List.of(Slot.item(part)),List.of(),Slot.molten(molten.id(),type.castAmount()),tr("melting",type.castAmount(),(60+20*type.cost)/20.0),tr("fuel"));
  }
  for(var h:level.getRecipeManager().getAllRecipesFor(Registration.ALLOY_RECIPE.get())){
   var r=h.value();if(Molten.get(r.result().molten())==null||r.inputs().stream().anyMatch(s->Molten.get(s.molten())==null))continue;
   add(out,Kind.ALLOYING,"alloy/"+h.id().getNamespace()+"/"+h.id().getPath(),r.inputs().stream().map(s->Slot.molten(s.molten(),s.amount())).toList(),List.of(),Slot.molten(r.result().molten(),r.result().amount()),tr("alloy"),tr("fuel"));
  }
  for(var molten:Molten.all()){
   var casts=new ArrayList<ItemStack>();casts.add(ItemStack.EMPTY);casts.add(new ItemStack(Registration.INGOT_CAST.get()));
   for(var cast:Registration.CASTS.values())casts.add(new ItemStack(cast.get()));
   for(int i=0;i<casts.size();i++){
    var cast=casts.get(i);var plan=SmelteryBlockEntity.castPlan(cast,molten);if(plan==null)continue;
    add(out,Kind.CASTING,"cast/"+molten.id()+"/"+i,List.of(Slot.molten(molten.id(),plan.amount())),cast.isEmpty()?List.of():List.of(Slot.item(cast)),Slot.item(plan.result()),tr("casting",plan.amount(),SmelteryBlockEntity.CAST_TIME/20.0),tr(cast.isEmpty()?"no_cast":"cast_kept"));
   }
  }
  for(var type:PartType.values()){
   var pattern=new ItemStack(Registration.PATTERNS.get(type).get());var plan=SmelteryBlockEntity.castPlan(pattern,Molten.GOLD);
   add(out,Kind.CASTING,"make_cast/"+type.id(),List.of(Slot.molten("gold",plan.amount()),Slot.item(pattern)),List.of(),Slot.item(plan.result()),tr("make_cast"),tr("pattern_consumed"));
  }
  var ingots=net.neoforged.neoforge.common.Tags.Items.INGOTS;
  var plan=SmelteryBlockEntity.castPlan(new ItemStack(Items.IRON_INGOT),Molten.GOLD);
  add(out,Kind.CASTING,"make_ingot_cast",List.of(Slot.molten("gold",plan.amount()),Slot.ingredient(Ingredient.of(ingots),1)),List.of(),Slot.item(plan.result()),tr("make_cast"),tr("ingot_consumed"));
  return List.copyOf(out);
 }
}
