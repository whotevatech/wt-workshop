package net.wtworkshop.smeltery;

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
import net.wtworkshop.WorkshopConfig;

public record MeltingRecipe(Ingredient ingredient, String molten, int amount, boolean ore, int time) implements Recipe<SingleRecipeInput> {
   public int outputAmount() {
      return this.ore ? (int)Math.round((double)this.amount * WorkshopConfig.oreMultiplier()) : this.amount;
   }

   public static Optional<MeltingRecipe> find(Level level, ItemStack stack) {
      return stack.isEmpty()
         ? Optional.empty()
         : level.getRecipeManager().getRecipeFor(Registration.MELTING_RECIPE.get(), new SingleRecipeInput(stack), level).map(RecipeHolder::value);
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
      return (RecipeSerializer<?>)Registration.MELTING_SERIALIZER.get();
   }

   public RecipeType<?> getType() {
      return (RecipeType<?>)Registration.MELTING_RECIPE.get();
   }

   public static final class Serializer implements RecipeSerializer<MeltingRecipe> {
      private static final MapCodec<MeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
                  Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MeltingRecipe::ingredient),
                  Codec.STRING.fieldOf("molten").forGetter(MeltingRecipe::molten),
                  Codec.intRange(1, 100000).fieldOf("amount").forGetter(MeltingRecipe::amount),
                  Codec.BOOL.optionalFieldOf("ore", false).forGetter(MeltingRecipe::ore),
                  Codec.intRange(1, 12000).optionalFieldOf("time", 100).forGetter(MeltingRecipe::time)
               )
               .apply(i, MeltingRecipe::new)
      );
      private static final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> STREAM = StreamCodec.composite(
         Ingredient.CONTENTS_STREAM_CODEC,
         MeltingRecipe::ingredient,
         ByteBufCodecs.STRING_UTF8,
         MeltingRecipe::molten,
         ByteBufCodecs.VAR_INT,
         MeltingRecipe::amount,
         ByteBufCodecs.BOOL,
         MeltingRecipe::ore,
         ByteBufCodecs.VAR_INT,
         MeltingRecipe::time,
         MeltingRecipe::new
      );

      public MapCodec<MeltingRecipe> codec() {
         return CODEC;
      }

      public StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec() {
         return STREAM;
      }
   }
}
