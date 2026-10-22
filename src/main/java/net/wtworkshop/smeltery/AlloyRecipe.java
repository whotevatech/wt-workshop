package net.wtworkshop.smeltery;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.wtworkshop.Registration;

public record AlloyRecipe(List<MoltenStack> inputs, MoltenStack result) implements Recipe<SingleRecipeInput> {
   public boolean matches(SingleRecipeInput input, Level level) {
      return false;
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
      return (RecipeSerializer<?>)Registration.ALLOY_SERIALIZER.get();
   }

   public RecipeType<?> getType() {
      return (RecipeType<?>)Registration.ALLOY_RECIPE.get();
   }

   public static final class Serializer implements RecipeSerializer<AlloyRecipe> {
      private static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
                  MoltenStack.CODEC.listOf(2, 8).fieldOf("inputs").forGetter(AlloyRecipe::inputs),
                  MoltenStack.CODEC.fieldOf("result").forGetter(AlloyRecipe::result)
               )
               .apply(i, AlloyRecipe::new)
      );
      private static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM = StreamCodec.composite(
         MoltenStack.STREAM_CODEC.apply(ByteBufCodecs.list()), AlloyRecipe::inputs, MoltenStack.STREAM_CODEC, AlloyRecipe::result, AlloyRecipe::new
      );

      public MapCodec<AlloyRecipe> codec() {
         return CODEC;
      }

      public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec() {
         return STREAM;
      }
   }
}
