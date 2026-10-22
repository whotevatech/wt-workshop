package net.wtworkshop.smeltery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.wtworkshop.Registration;
import net.wtworkshop.WorkshopConfig;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.part.CastItem;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.part.PatternItem;
import org.jetbrains.annotations.Nullable;

public class SmelteryBlockEntity extends BlockEntity implements MenuProvider {
   public static final int INPUTS = 3;
   public static final int FUEL = 3;
   public static final int CAST = 4;
   public static final int OUTPUT = 5;
   public static final int SLOTS = 6;
   public static final int MAX_FLUIDS = 8;
   public static final int CAST_TIME = 40;
   public static final int DATA_BURN = 0;
   public static final int DATA_BURN_MAX = 1;
   public static final int DATA_CAST = 2;
   public static final int DATA_MELT = 3;
   public static final int DATA_MELT_MAX = 6;
   public static final int DATA_COUNT = 9;
   public static final int DATA_FLUIDS = 10;
   public static final int DATA_CAPACITY = 26;
   public static final int DATA_SIZE = 27;
   private final ItemStackHandler items = new ItemStackHandler(6) {
      protected void onContentsChanged(int slot) {
         // Progress belongs to the current input, never the next item inserted.
         if (slot < INPUTS) {
            SmelteryBlockEntity.this.meltProgress[slot] = 0;
         }
         if (slot == CAST) {
            SmelteryBlockEntity.this.castProgress = 0;
         }
         SmelteryBlockEntity.this.setChanged();
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return SmelteryBlockEntity.this.level == null || SmelteryBlockEntity.validFor(slot, stack, SmelteryBlockEntity.this.level);
      }
   };
   private final List<SmelteryBlockEntity.Fluid> tank = new ArrayList<>();
   private final int[] meltProgress = new int[3];
   private final int[] meltTime = new int[3];
   private int burn;
   private int burnMax;
   private int castProgress;
   private final ContainerData data = new ContainerData() {
      public int get(int i) {
         if (i == 0) {
            return SmelteryBlockEntity.this.burn;
         } else if (i == 1) {
            return SmelteryBlockEntity.this.burnMax;
         } else if (i == 2) {
            return SmelteryBlockEntity.this.castProgress;
         } else if (i >= 3 && i < 6) {
            return SmelteryBlockEntity.this.meltProgress[i - 3];
         } else if (i >= 6 && i < 9) {
            return SmelteryBlockEntity.this.meltTime[i - 6];
         } else if (i == 9) {
            return SmelteryBlockEntity.this.tank.size();
         } else if (i == 26) {
            return WorkshopConfig.tankCapacity();
         } else {
            int f = (i - 10) / 2;
            if (f >= 0 && f < SmelteryBlockEntity.this.tank.size()) {
               return (i - 10) % 2 == 0 ? SmelteryBlockEntity.this.tank.get(f).molten.index() : SmelteryBlockEntity.this.tank.get(f).amount;
            } else {
               return 0;
            }
         }
      }

      public void set(int i, int value) {
      }

      public int getCount() {
         return 27;
      }
   };

   public SmelteryBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)Registration.SMELTERY_BE.get(), pos, state);
   }

   public static boolean validFor(int slot, ItemStack stack, Level level) {
      if (slot < 3) {
         return melt(stack, level).isPresent();
      } else if (slot == 3) {
         return stack.getBurnTime(RecipeType.SMELTING) > 0;
      } else {
         return slot != 4 ? false : stack.getItem() instanceof CastItem || stack.getItem() instanceof PatternItem || stack.is(Items.INGOTS);
      }
   }

   public static Optional<SmelteryBlockEntity.Melt> melt(ItemStack stack, Level level) {
      if (stack.getItem() instanceof PartItem part) {
         ToolMaterial m = PartItem.material(stack);
         Molten molten = m == null ? null : Molten.forMaterial(m.id());
         return molten == null ? Optional.empty() : Optional.of(new SmelteryBlockEntity.Melt(molten, part.type.castAmount(), 60 + 20 * part.type.cost));
      } else {
         return MeltingRecipe.find(level, stack).flatMap(r -> {
            Molten moltenx = Molten.get(r.molten());
            return moltenx == null ? Optional.empty() : Optional.of(new SmelteryBlockEntity.Melt(moltenx, r.outputAmount(), r.time()));
         });
      }
   }

   @Nullable
   public static SmelteryBlockEntity.CastPlan castPlan(ItemStack cast, @Nullable Molten molten) {
      if (molten == null) {
         return null;
      } else if (cast.isEmpty()) {
         return molten.block() == null ? null : new SmelteryBlockEntity.CastPlan(new ItemStack((ItemLike)molten.block().get()), molten.blockAmount(), false);
      } else if (cast.getItem() instanceof CastItem c) {
         if (c.type == null) {
            return new SmelteryBlockEntity.CastPlan(new ItemStack((ItemLike)molten.ingot().get()), 90, false);
         } else {
            ToolMaterial m = molten.material() == null ? null : Materials.get(molten.material());
            return m != null && m.supports(c.type.role) ? new SmelteryBlockEntity.CastPlan(PartItem.create(c.type, m), c.amount(), false) : null;
         }
      } else if (molten != Molten.GOLD) {
         return null;
      } else if (cast.getItem() instanceof PatternItem p) {
         return new SmelteryBlockEntity.CastPlan(new ItemStack((ItemLike)Registration.CASTS.get(p.type).get()), 180, true);
      } else {
         return cast.is(Items.INGOTS) ? new SmelteryBlockEntity.CastPlan(new ItemStack((ItemLike)Registration.INGOT_CAST.get()), 180, true) : null;
      }
   }

   public List<SmelteryBlockEntity.Fluid> tank() {
      return this.tank;
   }

   public int stored() {
      int total = 0;

      for (SmelteryBlockEntity.Fluid f : this.tank) {
         total += f.amount;
      }

      return total;
   }

   private boolean canFill(Molten molten, int amount) {
      return this.stored() + amount > WorkshopConfig.tankCapacity() ? false : this.tank.size() < 8 || this.tank.stream().anyMatch(f -> f.molten == molten);
   }

   private void fill(Molten molten, int amount) {
      for (SmelteryBlockEntity.Fluid f : this.tank) {
         if (f.molten == molten) {
            f.amount += amount;
            return;
         }
      }

      this.tank.add(new SmelteryBlockEntity.Fluid(molten, amount));
   }

   private int amountOf(Molten molten) {
      for (SmelteryBlockEntity.Fluid f : this.tank) {
         if (f.molten == molten) {
            return f.amount;
         }
      }

      return 0;
   }

   private void drain(Molten molten, int amount) {
      for (int i = 0; i < this.tank.size(); i++) {
         SmelteryBlockEntity.Fluid f = this.tank.get(i);
         if (f.molten == molten) {
            f.amount -= amount;
            if (f.amount <= 0) {
               this.tank.remove(i);
            }

            return;
         }
      }
   }

   public void select(int index) {
      if (index > 0 && index < this.tank.size()) {
         this.tank.add(0, this.tank.remove(index));
         this.castProgress = 0;
         this.setChanged();
      }
   }

   public void dump(int index) {
      if (index >= 0 && index < this.tank.size()) {
         this.tank.remove(index);
         this.castProgress = 0;
         this.setChanged();
      }
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, SmelteryBlockEntity be) {
      be.tick(level, pos, state);
   }

   private void tick(Level level, BlockPos pos, BlockState state) {
      boolean changed = false;
      boolean wasLit = this.burn > 0;
      SmelteryBlockEntity.Melt[] melts = new SmelteryBlockEntity.Melt[3];
      boolean wantsHeat = false;

      for (int i = 0; i < 3; i++) {
         ItemStack stack = this.items.getStackInSlot(i);
         melts[i] = stack.isEmpty() ? null : melt(stack, level).orElse(null);
         this.meltTime[i] = melts[i] == null ? 0 : melts[i].time();
         if (melts[i] == null) {
            this.meltProgress[i] = 0;
         } else if (this.canFill(melts[i].molten(), melts[i].amount())) {
            wantsHeat = true;
         }
      }

      List<AlloyRecipe> alloys = this.possibleAlloys(level);
      if (!alloys.isEmpty()) {
         wantsHeat = true;
      }

      if (this.burn <= 0 && wantsHeat) {
         ItemStack fuel = this.items.getStackInSlot(3);
         int time = fuel.getBurnTime(RecipeType.SMELTING);
         if (time > 0) {
            this.burn = this.burnMax = Math.min(time, 30000);
            ItemStack remainder = fuel.hasCraftingRemainingItem() ? fuel.getCraftingRemainingItem() : ItemStack.EMPTY;
            fuel.shrink(1);
            if (fuel.isEmpty()) {
               this.items.setStackInSlot(3, remainder);
            } else if (!remainder.isEmpty()) {
               Containers.dropItemStack(level, (double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5, remainder);
            }

            changed = true;
         }
      }

      if (this.burn > 0) {
         this.burn--;

         for (int ix = 0; ix < 3; ix++) {
            SmelteryBlockEntity.Melt m = melts[ix];
            if (m != null && this.canFill(m.molten(), m.amount())) {
               if (++this.meltProgress[ix] >= m.time()) {
                  this.meltProgress[ix] = 0;
                  this.fill(m.molten(), m.amount());
                  this.items.extractItem(ix, 1, false);
               }

               changed = true;
            }
         }

         for (AlloyRecipe r : alloys) {
            for (MoltenStack in : r.inputs()) {
               this.drain(Molten.get(in.molten()), in.amount());
            }

            this.fill(Molten.get(r.result().molten()), r.result().amount());
            changed = true;
         }
      }

      changed |= this.tickCasting();
      boolean lit = this.burn > 0;
      if (lit != wasLit && state.hasProperty(SmelteryBlock.LIT)) {
         level.setBlock(pos, (BlockState)state.setValue(SmelteryBlock.LIT, lit), 3);
      }

      if (changed) {
         this.setChanged();
      }
   }

   private List<AlloyRecipe> possibleAlloys(Level level) {
      List<AlloyRecipe> out = new ArrayList<>();
      if (this.tank.size() < 2) {
         return out;
      } else {
         for (RecipeHolder<AlloyRecipe> holder : level.getRecipeManager().getAllRecipesFor(Registration.ALLOY_RECIPE.get())) {
            AlloyRecipe r = (AlloyRecipe)holder.value();
            Molten result = Molten.get(r.result().molten());
            if (result != null) {
               boolean ok = true;
               int used = 0;

               for (MoltenStack in : r.inputs()) {
                  Molten m = Molten.get(in.molten());
                  if (m == null || this.amountOf(m) < in.amount()) {
                     ok = false;
                     break;
                  }

                  used += in.amount();
               }

               if (ok) {
                  int after = this.stored() - used + r.result().amount();
                  boolean slot = this.tank.size() < 8 || this.amountOf(result) > 0;
                  if (after <= WorkshopConfig.tankCapacity() && slot) {
                     out.add(r);
                     break;
                  }
               }
            }
         }

         return out;
      }
   }

   private boolean tickCasting() {
      Molten selected = this.tank.isEmpty() ? null : this.tank.get(0).molten;
      SmelteryBlockEntity.CastPlan plan = castPlan(this.items.getStackInSlot(4), selected);
      if (plan != null && this.amountOf(selected) >= plan.amount() && this.outputFits(plan.result())) {
         if (++this.castProgress < 40) {
            return true;
         } else {
            this.castProgress = 0;
            this.drain(selected, plan.amount());
            this.insertOutput(plan.result());
            if (plan.consumesCast()) {
               this.items.extractItem(4, 1, false);
            }

            return true;
         }
      } else if (this.castProgress != 0) {
         this.castProgress = 0;
         return true;
      } else {
         return false;
      }
   }

   private boolean outputFits(ItemStack stack) {
      ItemStack current = this.items.getStackInSlot(5);
      return current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack) && current.getCount() + stack.getCount() <= current.getMaxStackSize();
   }

   private void insertOutput(ItemStack stack) {
      ItemStack current = this.items.getStackInSlot(5);
      if (current.isEmpty()) {
         this.items.setStackInSlot(5, stack.copy());
      } else {
         current.grow(stack.getCount());
      }

      this.setChanged();
   }

   public static IItemHandler itemHandler(SmelteryBlockEntity be, @Nullable Direction side) {
      if (side == null) {
         return be.items;
      } else {
         return switch (side) {
            case UP -> new RangedWrapper(be.items, 0, 3);
            case DOWN -> new RangedWrapper(be.items, 5, 6);
            default -> new RangedWrapper(be.items, 3, 4);
         };
      }
   }

   public ItemStackHandler items() {
      return this.items;
   }

   public void dropContents(Level level, BlockPos pos) {
      for (int i = 0; i < 6; i++) {
         Containers.dropItemStack(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), this.items.getStackInSlot(i));
      }
   }

   public Component getDisplayName() {
      return Component.translatable("block.wt_workshop.smeltery");
   }

   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return new SmelteryMenu(id, inventory, this, this.data);
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("items", this.items.serializeNBT(registries));
      ListTag fluids = new ListTag();

      for (SmelteryBlockEntity.Fluid f : this.tank) {
         CompoundTag t = new CompoundTag();
         t.putString("molten", f.molten.id());
         t.putInt("amount", f.amount);
         fluids.add(t);
      }

      tag.put("tank", fluids);
      tag.putInt("burn", this.burn);
      tag.putInt("burnMax", this.burnMax);
      tag.putIntArray("melt", this.meltProgress);
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.items.deserializeNBT(registries, tag.getCompound("items"));
      this.tank.clear();

      for (Tag t : tag.getList("tank", 10)) {
         CompoundTag c = (CompoundTag)t;
         Molten m = Molten.get(c.getString("molten"));
         if (m != null && c.getInt("amount") > 0) {
            this.tank.add(new SmelteryBlockEntity.Fluid(m, c.getInt("amount")));
         }
      }

      this.burn = tag.getInt("burn");
      this.burnMax = tag.getInt("burnMax");
      int[] melt = tag.getIntArray("melt");
      System.arraycopy(melt, 0, this.meltProgress, 0, Math.min(melt.length, 3));
   }

   public static record CastPlan(ItemStack result, int amount, boolean consumesCast) {
   }

   public static final class Fluid {
      public final Molten molten;
      public int amount;

      Fluid(Molten molten, int amount) {
         this.molten = molten;
         this.amount = amount;
      }
   }

   public static record Melt(Molten molten, int amount, int time) {
   }
}
