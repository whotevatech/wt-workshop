package net.wtworkshop.station;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.wtworkshop.Registration;

public class StationMenu extends AbstractContainerMenu {
   public static final int OUTPUT = 0;
   public static final int FIRST_INPUT = 1;
   public static final int FIRST_INVENTORY = 6;
   public static final int END = 42;
   public static final int[][] POSITIONS = new int[][]{{26, 49}, {78, 49}, {130, 49}, {26, 102}, {78, 102}, {148, 102}};
   private final ContainerLevelAccess access;
   private final Player player;
   private final SimpleContainer inputs = new SimpleContainer(5) {
      public void setChanged() {
         super.setChanged();
         StationMenu.this.slotsChanged(this);
      }
   };
   private final SimpleContainer output = new SimpleContainer(1);
   private boolean consuming;

   public StationMenu(int id, Inventory inventory) {
      this(id, inventory, ContainerLevelAccess.NULL);
   }

   public StationMenu(int id, Inventory inventory, ContainerLevelAccess access) {
      super((MenuType)Registration.STATION_MENU.get(), id);
      this.access = access;
      this.player = inventory.player;
      this.addSlot(new Slot(this.output, 0, POSITIONS[5][0], POSITIONS[5][1]) {
         public boolean mayPlace(ItemStack stack) {
            return false;
         }

         public boolean mayPickup(Player p) {
            if (StationMenu.this.output.isEmpty()) {
               return false;
            } else if (p.level().isClientSide) {
               return true;
            } else {
               StationLogic.Outcome now = StationLogic.compute(StationMenu.this.inputList(), p.level());
               return now.success() && ItemStack.isSameItemSameComponents(now.result(), this.getItem());
            }
         }

         public void onTake(Player p, ItemStack stack) {
            StationLogic.Outcome outcome = StationLogic.compute(StationMenu.this.inputList(), p.level());
            if (p instanceof net.minecraft.server.level.ServerPlayer serverPlayer && outcome.success()) net.wtworkshop.tool.WorkshopMilestones.station(serverPlayer, StationMenu.this.tool(), stack);
            StationMenu.this.consuming = true;

            try {
               for (int i = 0; i < 5; i++) {
                  if (outcome.consume()[i] > 0) {
                     var consumed=StationMenu.this.inputs.getItem(i);
                     var remainder=consumed.copyWithCount(1).getCraftingRemainingItem();
                     int count=outcome.consume()[i];
                     StationMenu.this.inputs.removeItem(i,count);
                     if(!p.level().isClientSide&&!remainder.isEmpty())for(int n=0;n<count;n++)p.getInventory().placeItemBackInInventory(remainder.copy());
                  }
               }
            } finally {
               StationMenu.this.consuming = false;
            }

            StationMenu.this.slotsChanged(StationMenu.this.inputs);
            super.onTake(p, stack);
         }
      });

      for (int i = 0; i < 5; i++) {
         final int role = i;
         this.addSlot(new Slot(this.inputs, i, POSITIONS[i][0], POSITIONS[i][1]) {
            public boolean mayPlace(ItemStack stack) {
               return StationLogic.accepts(role, stack);
            }

            public int getMaxStackSize() {
               return role == 3 ? 1 : super.getMaxStackSize();
            }
         });
      }

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 10 + col * 18, 152 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 10 + col * 18, 210));
      }
   }

   public List<ItemStack> inputList() {
      List<ItemStack> list = new ArrayList<>(5);

      for (int i = 0; i < 5; i++) {
         list.add(this.inputs.getItem(i));
      }

      return list;
   }

   public ItemStack tool() {
      return this.inputs.getItem(3);
   }

   public ItemStack preview() {
      return this.output.getItem(0);
   }

   public Player player() {
      return this.player;
   }

   public void slotsChanged(Container container) {
      if (!this.consuming && !this.player.level().isClientSide) {
         this.output.setItem(0, StationLogic.compute(this.inputList(), this.player.level()).result());
         this.broadcastChanges();
      }
   }

   public boolean stillValid(Player p) {
      return stillValid(this.access, p, (Block)Registration.TOOL_STATION.get());
   }

   public void removed(Player p) {
      super.removed(p);
      this.access.execute((level, pos) -> this.clearContainer(p, this.inputs));
   }

   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.container != this.output && super.canTakeItemForPickAll(stack, slot);
   }

   public ItemStack quickMoveStack(Player p, int index) {
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem() && slot.mayPickup(p)) {
         ItemStack current = slot.getItem();
         ItemStack copy = current.copy();
         if (index < 6) {
            if (!this.moveItemStackTo(current, 6, 42, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            boolean moved = false;

            for (int role = 0; role < 5 && !moved; role++) {
               if (StationLogic.accepts(role, current)) {
                  moved = this.moveItemStackTo(current, 1 + role, 1 + role + 1, false);
               }
            }

            if (!moved) {
               return ItemStack.EMPTY;
            }
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
   }
}
