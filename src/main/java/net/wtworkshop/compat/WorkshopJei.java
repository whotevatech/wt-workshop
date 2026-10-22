package net.wtworkshop.compat;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.*;
import mezz.jei.api.ingredients.subtypes.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.gui.builder.*;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.registration.*;
import net.wtworkshop.*;
import net.wtworkshop.smeltery.*;
@JeiPlugin
public final class WorkshopJei implements IModPlugin {
 public static final IIngredientType<MoltenStack> MOLTEN=()->MoltenStack.class;
 public static final Map<ViewerRecipes.Kind,RecipeType<ViewerRecipes.Display>> TYPES=new EnumMap<>(ViewerRecipes.Kind.class);
 public static int registeredCount;
 public static mezz.jei.api.runtime.IJeiRuntime runtime;
 private static boolean nativeEmi(){return net.neoforged.fml.ModList.get().isLoaded("emi");}
 public ResourceLocation getPluginUid(){return ResourceLocation.fromNamespaceAndPath("wt_workshop","jei");}
 @Override public void registerIngredients(IModIngredientRegistration r){
  if(nativeEmi())return;var values=Molten.all().stream().map(m->new MoltenStack(m.id(),90)).toList();
  // JEI added the codec overload after 19.21. Use it when available, while retaining older pack compatibility.
  try {var method=IModIngredientRegistration.class.getMethod("register",IIngredientType.class,java.util.Collection.class,IIngredientHelper.class,IIngredientRenderer.class,com.mojang.serialization.Codec.class);method.invoke(r,MOLTEN,values,new MoltenHelper(),new MoltenRenderer(),MoltenStack.CODEC);}
  catch(NoSuchMethodException olderJei){r.register(MOLTEN,values,new MoltenHelper(),new MoltenRenderer());}
  catch(ReflectiveOperationException failure){throw new IllegalStateException("Could not register molten workshop ingredients",failure);}
 }
 @Override public void registerItemSubtypes(ISubtypeRegistration r){if(nativeEmi())return;for(var p:Registration.PARTS.values())r.registerSubtypeInterpreter(p.get(),(IIngredientSubtypeInterpreter<ItemStack>)(stack,context)->stack.getOrDefault(Registration.PART_MATERIAL.get(),""));}
 @Override public void registerCategories(IRecipeCategoryRegistration r){
  TYPES.clear();if(nativeEmi())return;
  for(var kind:ViewerRecipes.Kind.values()){var type=RecipeType.create("wt_workshop",kind.id(),ViewerRecipes.Display.class);TYPES.put(kind,type);r.addRecipeCategories(new Category(kind,type,r.getJeiHelpers().getGuiHelper()));}
 }
 @Override public void registerRecipes(IRecipeRegistration r){
  if(nativeEmi())return;var recipes=ViewerRecipes.build(Minecraft.getInstance().level);registeredCount=recipes.size();for(var kind:ViewerRecipes.Kind.values())r.addRecipes(TYPES.get(kind),recipes.stream().filter(d->d.kind()==kind).toList());org.slf4j.LoggerFactory.getLogger("WT Workshop").info("Registered {} native JEI workshop recipes",registeredCount);
 }
 @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration r){if(nativeEmi())return;for(var kind:ViewerRecipes.Kind.values())r.addRecipeCatalyst(kind.icon(),TYPES.get(kind));}
 @Override public void registerGuiHandlers(IGuiHandlerRegistration r){if(nativeEmi())return;r.addRecipeClickArea(net.wtworkshop.client.BuilderScreen.class,112,52,24,22,TYPES.get(ViewerRecipes.Kind.PARTS),TYPES.get(ViewerRecipes.Kind.PATTERNS));r.addRecipeClickArea(net.wtworkshop.client.StationScreen.class,112,95,24,24,TYPES.get(ViewerRecipes.Kind.ASSEMBLY),TYPES.get(ViewerRecipes.Kind.MODIFIERS));}
 @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime r){runtime=r;}
 @Override public void onRuntimeUnavailable(){runtime=null;}
 public static final class MoltenHelper implements IIngredientHelper<MoltenStack>{
  public IIngredientType<MoltenStack> getIngredientType(){return MOLTEN;}
  public String getDisplayName(MoltenStack s){return Molten.get(s.molten()).displayName().getString();}
  public String getUniqueId(MoltenStack s,UidContext c){return "wt_workshop:molten/"+s.molten();}
  public ResourceLocation getResourceLocation(MoltenStack s){return ResourceLocation.fromNamespaceAndPath("wt_workshop","molten/"+s.molten());}
  public MoltenStack copyIngredient(MoltenStack s){return s;}
  public MoltenStack normalizeIngredient(MoltenStack s){return new MoltenStack(s.molten(),90);}
  public long getAmount(MoltenStack s){return s.amount();}
  public MoltenStack copyWithAmount(MoltenStack s,long amount){return new MoltenStack(s.molten(),(int)Math.min(1000000,Math.max(1,amount)));}
  public boolean isValidIngredient(MoltenStack s){return s!=null&&s.amount()>0&&Molten.get(s.molten())!=null;}
  public String getErrorInfo(MoltenStack s){return String.valueOf(s);}
 }
 public static final class MoltenRenderer implements IIngredientRenderer<MoltenStack>{
  public void render(GuiGraphics g,MoltenStack s){ViewerDrawing.molten(g,0,0,s);}
  public List<Component> getTooltip(MoltenStack s,TooltipFlag flag){return ViewerDrawing.tooltip(s);}
 }
 public static final class Category implements IRecipeCategory<ViewerRecipes.Display>{
  private final ViewerRecipes.Kind kind;private final RecipeType<ViewerRecipes.Display> type;private final IDrawable icon,background;
  public Category(ViewerRecipes.Kind k,RecipeType<ViewerRecipes.Display> t,mezz.jei.api.helpers.IGuiHelper gui){kind=k;type=t;icon=gui.createDrawableItemStack(k.icon());background=gui.createBlankDrawable(ViewerDrawing.WIDTH,ViewerDrawing.height(k));}
  public RecipeType<ViewerRecipes.Display> getRecipeType(){return type;}
  public Component getTitle(){return kind.title();}
  public IDrawable getIcon(){return icon;}
  public IDrawable getBackground(){return background;}
  public ResourceLocation getRegistryName(ViewerRecipes.Display r){return r.id();}
  public List<ResourceLocation> getSourceRecipeIds(ViewerRecipes.Display r){return List.of();}
  private static void fill(IRecipeSlotBuilder slot,ViewerRecipes.Slot s){if(s.molten()!=null)slot.addIngredient(MOLTEN,s.molten());else slot.addItemStacks(s.items());}
  public void setRecipe(IRecipeLayoutBuilder b,ViewerRecipes.Display r,IFocusGroup focuses){int i=0;for(var s:r.inputs()){fill(b.addSlot(RecipeIngredientRole.INPUT,4+i%4*23,4+i/4*23).setStandardSlotBackground(),s);i++;}for(var s:r.catalysts()){fill(b.addSlot(RecipeIngredientRole.CATALYST,4+i%4*23,4+i/4*23).setStandardSlotBackground().addTooltipCallback((view,lines)->lines.add(Component.translatable("viewer.wt_workshop.reusable"))),s);i++;}fill(b.addSlot(RecipeIngredientRole.OUTPUT,152,4).setOutputSlotBackground(),r.output());}
  public void draw(ViewerRecipes.Display r,IRecipeSlotsView slots,GuiGraphics g,double mx,double my){ViewerDrawing.background(g,ViewerDrawing.height(kind));ViewerDrawing.notes(g,r);}
 }
}
