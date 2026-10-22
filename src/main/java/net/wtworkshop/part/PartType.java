package net.wtworkshop.part;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.wtworkshop.material.PartRole;

public enum PartType {
   PICKAXE_HEAD(PartRole.HEAD, 3),
   AXE_HEAD(PartRole.HEAD, 3),
   SHOVEL_HEAD(PartRole.HEAD, 1),
   HOE_HEAD(PartRole.HEAD, 2),
   SWORD_BLADE(PartRole.HEAD, 2),
   HAMMER_HEAD(PartRole.HEAD, 8),
   EXCAVATOR_HEAD(PartRole.HEAD, 8),
   BROAD_AXE_HEAD(PartRole.HEAD, 8),
   HANDLE(PartRole.HANDLE, 1),
   TOUGH_HANDLE(PartRole.HANDLE, 3),
   BINDING(PartRole.BINDING, 1),
   GUARD(PartRole.BINDING, 1),
   TOUGH_BINDING(PartRole.BINDING, 3);

   public final PartRole role;
   public final int cost;

   private PartType(PartRole role, int cost) {
      this.role = role;
      this.cost = cost;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public Component displayName() {
      return Component.translatable("item.wt_workshop." + this.id());
   }

   public int castAmount() {
      return this.cost * 90;
   }

   public static PartType byId(String id) {
      for (PartType t : values()) {
         if (t.id().equals(id)) {
            return t;
         }
      }

      return null;
   }
}
