package net.wtworkshop.compat;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.api.widget.WidgetHolder;
import net.wtworkshop.*;
import net.wtworkshop.part.*;
import net.wtworkshop.smeltery.*;
@EmiEntrypoint
public final class WorkshopEmi implements EmiPlugin {
 public static final Map<ViewerRecipes.Kind,EmiRecipeCategory> CATEGORIES=new EnumMap<>(ViewerRecipes.Kind.class);
 public static int registeredCount;
 @Override public void initialize(EmiInitRegistry registry){registry.addIngredientSerializer(MoltenEmiStack.class,new Serializer());}
 @Override public void register(EmiRegistry registry){
  CATEGORIES.clear();for(var kind:ViewerRecipes.Kind.values()){var category=new EmiRecipeCategory(ResourceLocation.fromNamespaceAndPath("wt_workshop",kind.id()),EmiStack.of(kind.icon())){@Override public Component getName(){return kind.title();}};CATEGORIES.put(kind,category);registry.addCategory(category);registry.addWorkstation(category,EmiStack.of(kind.icon()));}
  for(var molten:Molten.all())registry.addEmiStack(new MoltenEmiStack(molten.id(),90));
  for(var part:Registration.PARTS.values())registry.setDefaultComparison(part.get(),Comparison.compareData(s->s.getItemStack().get(Registration.PART_MATERIAL.get())));
  for(var part:PartType.values())for(var m:net.wtworkshop.material.Materials.all())if(m.supports(part.role))registry.addEmiStack(EmiStack.of(PartItem.create(part,m)));
  var recipes=ViewerRecipes.build(Minecraft.getInstance().level);registeredCount=recipes.size();for(var recipe:recipes)registry.addRecipe(new Recipe(recipe));
  org.slf4j.LoggerFactory.getLogger("WT Workshop").info("Registered {} native EMI workshop recipes",registeredCount);
 }
 public static EmiIngredient ingredient(ViewerRecipes.Slot s){if(s.molten()!=null)return new MoltenEmiStack(s.molten().molten(),s.molten().amount());return EmiIngredient.of(s.items().stream().map(i->EmiStack.of(i.copyWithCount(1))).toList(),s.items().isEmpty()?1:s.items().getFirst().getCount());}
 public static final class Recipe extends BasicEmiRecipe {
  private final ViewerRecipes.Display recipe;
  public Recipe(ViewerRecipes.Display r){super(CATEGORIES.get(r.kind()),r.id(),ViewerDrawing.WIDTH,ViewerDrawing.height(r.kind()));recipe=r;for(var s:r.inputs())inputs.add(ingredient(s));for(var s:r.catalysts())catalysts.add(ingredient(s));outputs.add(r.output().molten()!=null?new MoltenEmiStack(r.output().molten().molten(),r.output().molten().amount()):EmiStack.of(r.output().items().getFirst()));}
  @Override public boolean supportsRecipeTree(){return recipe.kind()!=ViewerRecipes.Kind.MODIFIERS;}
  @Override public void addWidgets(WidgetHolder widgets){
   widgets.addDrawable(0,0,width,height,(g,mx,my,delta)->{ViewerDrawing.background(g,height);ViewerDrawing.notes(g,recipe);});
   int i=0;for(var s:inputs){widgets.addSlot(s,3+i%4*23,3+i/4*23);i++;}for(var s:catalysts){widgets.addSlot(s,3+i%4*23,3+i/4*23).catalyst(true).appendTooltip(Component.translatable("viewer.wt_workshop.reusable"));i++;}widgets.addSlot(outputs.getFirst(),151,3).recipeContext(this);
  }
 }
 public static final class MoltenEmiStack extends EmiStack {
  private final String molten;
  public MoltenEmiStack(String molten,long amount){this.molten=molten;this.amount=amount;}
  @Override public EmiStack copy(){return new MoltenEmiStack(molten,amount).setChance(chance).comparison(comparison);}
  @Override public boolean isEmpty(){return amount<=0||Molten.get(molten)==null;}
  @Override public DataComponentPatch getComponentChanges(){return DataComponentPatch.EMPTY;}
  @Override public Object getKey(){return Molten.get(molten);}
  @Override public ResourceLocation getId(){return ResourceLocation.fromNamespaceAndPath("wt_workshop","molten/"+molten);}
  @Override public Component getName(){return Molten.get(molten).displayName();}
  @Override public List<Component> getTooltipText(){return ViewerDrawing.tooltip(new MoltenStack(molten,(int)amount));}
  @Override public void render(GuiGraphics g,int x,int y,float delta,int flags){if((flags&EmiIngredient.RENDER_ICON)!=0)ViewerDrawing.molten(g,x,y,new MoltenStack(molten,(int)amount));}
 }
 private static final class Serializer implements EmiIngredientSerializer<MoltenEmiStack>{
  public String getType(){return "wt_workshop:molten";}
  public EmiIngredient deserialize(JsonElement json){try{var o=json.getAsJsonObject();String id=o.get("molten").getAsString();long amount=o.has("amount")?o.get("amount").getAsLong():90;return Molten.get(id)==null||amount<=0?EmiStack.EMPTY:new MoltenEmiStack(id,amount);}catch(RuntimeException e){return EmiStack.EMPTY;}}
  public JsonElement serialize(MoltenEmiStack stack){var o=new JsonObject();o.addProperty("type",getType());o.addProperty("molten",stack.molten);o.addProperty("amount",stack.getAmount());return o;}
 }
}
