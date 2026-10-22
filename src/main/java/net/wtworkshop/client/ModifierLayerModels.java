package net.wtworkshop.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.wtworkshop.tool.*;
/** Additive render passes avoid a separate baked model for every modifier combination. */
public final class ModifierLayerModels extends BakedModelWrapper<BakedModel> {
 private final BakedModel[] layers;
 private final Map<String,BakedModel> addons;
 private final Map<Integer,List<BakedModel>> combinations=new HashMap<>();
 public ModifierLayerModels(BakedModel base,BakedModel[] layers,Map<String,BakedModel> addons){super(base);this.layers=layers.clone();this.addons=Map.copyOf(addons);}
 public static ModelResourceLocation layer(ToolType type,int index){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("wt_workshop","item/modifier_layers/"+type.id()+"_"+ModifierAppearance.STYLES.get(index).id()));}
 @Override public ItemOverrides getOverrides(){return ItemOverrides.EMPTY;}
 @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){originalModel.applyTransform(context,pose,left);return this;}
 @Override public List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous){var base=combinations.computeIfAbsent(ModifierAppearance.flags(stack),flags->{var out=new ArrayList<BakedModel>();out.add(originalModel);for(int i=0;i<layers.length;i++)if((flags&(1<<i))!=0)out.add(layers[i]);return List.copyOf(out);});var d=WorkshopTool.data(stack);if(d==null||addons.isEmpty())return base;var passes=new ArrayList<BakedModel>(base);addons.forEach((id,model)->{if(d.modifierLevel(id)>0)passes.add(model);});return List.copyOf(passes);}
}
