package net.wtworkshop.builder;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.wtworkshop.Registration;
import net.wtworkshop.part.PartType;
import net.wtworkshop.part.PatternItem;

public class PartBuilderMenu extends AbstractContainerMenu {
   public static final int OUTPUT = 0;
   public static final int PATTERN = 1;
   public static final int MATERIAL = 2;
   public static final int FIRST_INVENTORY = 3;
   public static final int END = 39;
   private final Player player;
   private final ContainerLevelAccess access;
   private final DataSlot selected = DataSlot.standalone();
   private boolean consuming;
   private final SimpleContainer inputs = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         PartBuilderMenu.this.slotsChanged(this);
      }
   };
   private final SimpleContainer output = new SimpleContainer(1);

   public PartBuilderMenu(int id, Inventory inventory) {
      this(id, inventory, ContainerLevelAccess.NULL);
   }

   public PartBuilderMenu(int id, Inventory inventory, ContainerLevelAccess access) {
      super((MenuType)Registration.PART_BUILDER_MENU.get(), id);
      this.player = inventory.player;
      this.access = access;
      this.selected.set(-1);
      this.addDataSlot(this.selected);
      this.addSlot(new Slot(this.output, 0, 143, 57) {
         public boolean mayPlace(ItemStack stack) {
            return false;
         }

         public boolean mayPickup(Player p) {
            if (PartBuilderMenu.this.output.isEmpty()) {
               return false;
            } else if (p.level().isClientSide) {
               return true;
            } else {
               PartBuilderLogic.Outcome now = PartBuilderMenu.this.compute();
               return now.success() && ItemStack.isSameItemSameComponents(now.result(), this.getItem());
            }
         }

         public void onTake(Player p, ItemStack stack) {
            PartBuilderLogic.Outcome outcome = PartBuilderMenu.this.compute();
            PartBuilderMenu.this.consuming = true;

            try {
               if (outcome.patternCost() > 0) {
                  PartBuilderMenu.this.inputs.removeItem(0, outcome.patternCost());
               }

               if (outcome.materialCost() > 0) {
                  PartBuilderMenu.this.inputs.removeItem(1, outcome.materialCost());
               }
            } finally {
               PartBuilderMenu.this.consuming = false;
            }

            PartBuilderMenu.this.slotsChanged(PartBuilderMenu.this.inputs);
            super.onTake(p, stack);
         }
      });
      this.addSlot(new Slot(this.inputs, 0, 25, 57) {
         public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof PatternItem || stack.is((Item)Registration.BLANK_PATTERN.get());
         }
      });
      this.addSlot(new Slot(this.inputs, 1, 81, 57) {
         public boolean mayPlace(ItemStack stack) {
            return !(stack.getItem() instanceof PatternItem) && !stack.is((Item)Registration.BLANK_PATTERN.get());
         }
      });

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 10 + col * 18, 140 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 10 + col * 18, 198));
      }
   }

   public PartBuilderLogic.Outcome compute() {
      return PartBuilderLogic.compute(this.inputs.getItem(0), this.inputs.getItem(1), this.selected.get(), this.player.level());
   }

   public Player player() {
      return this.player;
   }

   public ItemStack pattern() {
      return this.inputs.getItem(0);
   }

   public ItemStack material() {
      return this.inputs.getItem(1);
   }

   public int selected() {
      return this.selected.get();
   }

   public boolean carving() {
      return this.pattern().is((Item)Registration.BLANK_PATTERN.get());
   }

   public boolean clickMenuButton(Player p, int id) {
      if (id >= 0 && id < PartType.values().length) {
         this.selected.set(id);
         this.slotsChanged(this.inputs);
         return true;
      } else {
         return false;
      }
   }

   public void slotsChanged(Container container) {
      if (!this.consuming && !this.player.level().isClientSide) {
         this.output.setItem(0, this.compute().result());
         this.broadcastChanges();
      }
   }

   public boolean stillValid(Player p) {
      return stillValid(this.access, p, (Block)Registration.PART_BUILDER.get());
   }

   public void removed(Player p) {
      super.removed(p);
      this.access.execute((level, pos) -> this.clearContainer(p, this.inputs));
   }

   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.container != this.output && super.canTakeItemForPickAll(stack, slot);
   }

   public ItemStack quickMoveStack(Player p, int index) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = (Slot)this.slots.get(index);
         if (slot.hasItem() && slot.mayPickup(p)) {
            ItemStack current = slot.getItem();
            ItemStack copy = current.copy();
            if (index < 3) {
               if (!this.moveItemStackTo(current, 3, 39, true)) {
                  return ItemStack.EMPTY;
               }
            } else if (((Slot)this.slots.get(1)).mayPlace(current)) {
               if (!this.moveItemStackTo(current, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(current, 2, 3, false)) {
               return ItemStack.EMPTY;
            }

            if (current.isEmpty()) {
               slot.setByPlayer(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            if (current.getCount() == copy.getCount()) {
               return ItemStack.EMPTY;
            } else {
               slot.onTake(p, copy);
               return copy;
            }
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }
}
