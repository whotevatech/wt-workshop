package net.wtworkshop.api;
import java.util.*;
import java.util.function.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.ChatFormatting;
import net.wtworkshop.tool.*;
/** Register once during addon construction, identically on client and server. */
public record AddonModifier(ResourceLocation key,Modifier.Slot slot,IntSupplier maximumLevel,int pointsPerLevel,
 Supplier<Ingredient> ingredient,Predicate<ToolType> allowed,Supplier<Component> description,ChatFormatting colour,
 Set<String> conflicts,DoubleSupplier durabilitySavingPerLevel,Function<ToolType,ResourceLocation> overlayModel) {
 public AddonModifier {Objects.requireNonNull(key);Objects.requireNonNull(slot);Objects.requireNonNull(maximumLevel);Objects.requireNonNull(ingredient);Objects.requireNonNull(allowed);Objects.requireNonNull(description);Objects.requireNonNull(colour);Objects.requireNonNull(durabilitySavingPerLevel);Objects.requireNonNull(overlayModel);conflicts=Set.copyOf(conflicts);if(pointsPerLevel<1)throw new IllegalArgumentException("pointsPerLevel must be positive");}
 public String id(){return key.toString();}
 public int maxLevel(){return Math.max(0,maximumLevel.getAsInt());}
 public int valueOf(ItemStack item){return !item.isEmpty()&&ingredient.get().test(item)?1:0;}
 public Component displayName(int level){return Component.translatable("wt_workshop.level_suffix",Component.translatable("modifier."+key.getNamespace()+"."+key.getPath()),Component.translatable("enchantment.level."+level)).withStyle(colour);}
}
