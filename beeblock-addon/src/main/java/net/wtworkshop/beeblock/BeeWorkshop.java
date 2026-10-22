package net.wtworkshop.beeblock;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.wtworkshop.api.*;
import net.wtworkshop.tool.*;
@Mod("wt_beeblock_workshop")
public final class BeeWorkshop {
 public static final String WAXED="wt_beeblock_workshop:waxed",HONEYBOUND="wt_beeblock_workshop:honeybound";
 public static final ModConfigSpec SPEC;private static final ModConfigSpec.IntValue WAX_MAX,HONEY_MAX,DURATION,COOLDOWN;
 private static final ModConfigSpec.DoubleValue WAX_SAVE;private static final ModConfigSpec.BooleanValue PVP;
 static {var b=new ModConfigSpec.Builder();b.push("waxed");WAX_MAX=b.defineInRange("maximumLevel",3,0,5);WAX_SAVE=b.comment("Chance per level to prevent remaining wear. Combined tool saving chance is capped at 90%.").defineInRange("savingChancePerLevel",0.1,0.0,0.3);b.pop();b.push("honeybound");HONEY_MAX=b.defineInRange("maximumLevel",2,0,3);DURATION=b.comment("Slowness duration after real melee health damage.").defineInRange("durationTicks",40,1,200);COOLDOWN=b.defineInRange("cooldownTicks",20,0,200);PVP=b.define("affectsPlayers",false);b.pop();SPEC=b.build();}
 public static int waxMax(){return SPEC.isLoaded()?WAX_MAX.get():WAX_MAX.getDefault();}
 public static int honeyMax(){return SPEC.isLoaded()?HONEY_MAX.get():HONEY_MAX.getDefault();}
 public static double saving(){return SPEC.isLoaded()?WAX_SAVE.get():WAX_SAVE.getDefault();}
 public static int duration(){return SPEC.isLoaded()?DURATION.get():DURATION.getDefault();}
 public static int cooldown(){return SPEC.isLoaded()?COOLDOWN.get():COOLDOWN.getDefault();}
 public static boolean pvp(){return SPEC.isLoaded()?PVP.get():PVP.getDefault();}
 private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("wt_beeblock_workshop",path);}
 public BeeWorkshop(ModContainer mod){
  mod.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,SPEC);
  AddonModifiers.register(new AddonModifier(id("waxed"),Modifier.Slot.UPGRADE,BeeWorkshop::waxMax,8,()->Ingredient.of(TagKey.create(Registries.ITEM,id("wax"))),t->true,()->Component.translatable("modifier.wt_beeblock_workshop.waxed.desc",net.wtworkshop.util.Text.number(saving()*100)),ChatFormatting.YELLOW,Set.of(),BeeWorkshop::saving,t->id("item/modifier_layers/"+t.id()+"_waxed")));
  AddonModifiers.register(new AddonModifier(id("honeybound"),Modifier.Slot.ABILITY,BeeWorkshop::honeyMax,8,()->Ingredient.of(TagKey.create(Registries.ITEM,id("honey"))),t->t==ToolType.SWORD||t==ToolType.AXE||t==ToolType.BROAD_AXE,()->Component.translatable("modifier.wt_beeblock_workshop.honeybound.desc",net.wtworkshop.util.Text.number(duration()/20.0)),ChatFormatting.GOLD,Set.of(),()->0,t->id("item/modifier_layers/"+t.id()+"_honeybound")));
 }
}
