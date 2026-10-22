package net.wtworkshop.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.wtworkshop.Registration;
import org.jetbrains.annotations.Nullable;

public record MaterialRecipe(Ingredient ingredient, String material, int value, int needed) implements Recipe<SingleRecipeInput> {
   @Nullable
   public ToolMaterial toolMaterial() {
      return Materials.get(this.material);
   }

   public int units(int count) {
      return count * this.value / this.needed;
   }

   public int itemsFor(int units) {
      return (units * this.needed + this.value - 1) / this.value;
   }

   public static Optional<MaterialRecipe> find(Level level, ItemStack stack) {
      return stack.isEmpty()
         ? Optional.empty()
         : level.getRecipeManager().getRecipeFor(Registration.MATERIAL_RECIPE.get(), new SingleRecipeInput(stack), level).map(RecipeHolder::value);
   }

   public boolean matches(SingleRecipeInput input, Level level) {
      return this.ingredient.test(input.item());
   }

   public ItemStack assemble(SingleRecipeInput input, Provider registries) {
      return ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return true;
   }

   public ItemStack getResultItem(Provider registries) {
      return ItemStack.EMPTY;
   }

   public boolean isSpecial() {
      return true;
   }

   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)Registration.MATERIAL_SERIALIZER.get();
   }

   public RecipeType<?> getType() {
      return (RecipeType<?>)Registration.MATERIAL_RECIPE.get();
   }

   public static final class Serializer implements RecipeSerializer<MaterialRecipe> {
      private static final MapCodec<MaterialRecipe> CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
                  Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MaterialRecipe::ingredient),
                  Codec.STRING.fieldOf("material").forGetter(MaterialRecipe::material),
                  Codec.intRange(1, 1024).optionalFieldOf("value", 1).forGetter(MaterialRecipe::value),
                  Codec.intRange(1, 1024).optionalFieldOf("needed", 1).forGetter(MaterialRecipe::needed)
               )
               .apply(i, MaterialRecipe::new)
      );
      private static final StreamCodec<RegistryFriendlyByteBuf, MaterialRecipe> STREAM = StreamCodec.composite(
         Ingredient.CONTENTS_STREAM_CODEC,
         MaterialRecipe::ingredient,
         ByteBufCodecs.STRING_UTF8,
         MaterialRecipe::material,
         ByteBufCodecs.VAR_INT,
         MaterialRecipe::value,
         ByteBufCodecs.VAR_INT,
         MaterialRecipe::needed,
         MaterialRecipe::new
      );

      public MapCodec<MaterialRecipe> codec() {
         return CODEC;
      }

      public StreamCodec<RegistryFriendlyByteBuf, MaterialRecipe> streamCodec() {
         return STREAM;
      }
   }
}
