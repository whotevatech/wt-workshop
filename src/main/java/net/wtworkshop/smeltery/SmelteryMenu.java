package net.wtworkshop.smeltery;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.wtworkshop.Registration;
import org.jetbrains.annotations.Nullable;

public class SmelteryMenu extends AbstractContainerMenu {
   public static final int FIRST_INVENTORY = 6;
   public static final int END = 42;
   public static final int BUTTON_DUMP = 100;
   public static final int[][] POSITIONS = new int[][]{{20, 30}, {20, 52}, {20, 74}, {20, 108}, {142, 52}, {142, 92}};
   @Nullable
   private final SmelteryBlockEntity be;
   private final ContainerData data;
   private final ContainerLevelAccess access;
   private final Player player;

   public SmelteryMenu(int id, Inventory inventory) {
      this(id, inventory, null, new SimpleContainerData(27));
   }

   public SmelteryMenu(int id, Inventory inventory, @Nullable SmelteryBlockEntity be, ContainerData data) {
      super((MenuType)Registration.SMELTERY_MENU.get(), id);
      this.be = be;
      this.data = data;
      this.player = inventory.player;
      this.access = be == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
      IItemHandler handler = be != null ? be.items() : new ItemStackHandler(6) {
         public boolean isItemValid(int slot, ItemStack stack) {
            return SmelteryBlockEntity.validFor(slot, stack, SmelteryMenu.this.player.level());
         }
      };

      for (int i = 0; i < 6; i++) {
         this.addSlot(new SlotItemHandler(handler, i, POSITIONS[i][0], POSITIONS[i][1]));
      }

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 8 + col * 18, 198));
      }

      this.addDataSlots(data);
   }

   public int data(int index) {
      return this.data.get(index);
   }

   public boolean clickMenuButton(Player p, int id) {
      if (this.be == null) {
         return true;
      } else {
         if (id >= 100 && id < 108) {
            this.be.dump(id - 100);
         } else {
            if (id < 0 || id >= 8) {
               return false;
            }

            this.be.select(id);
         }

         return true;
      }
   }

   public boolean stillValid(Player p) {
      return this.be == null || stillValid(this.access, p, (Block)Registration.SMELTERY.get());
   }

   public ItemStack quickMoveStack(Player p, int index) {
      Slot slot = (Slot)this.slots.get(index);
      if (!slot.hasItem()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack current = slot.getItem();
         ItemStack copy = current.copy();
         if (index < 6) {
            if (!this.moveItemStackTo(current, 6, 42, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            boolean moved = false;
            int[][] order = new int[][]{{0, 3}, {4, 5}, {3, 4}};

            for (int[] range : order) {
               if (!moved && ((Slot)this.slots.get(range[0])).mayPlace(current)) {
                  moved = this.moveItemStackTo(current, range[0], range[1], false);
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
      }
   }
}
