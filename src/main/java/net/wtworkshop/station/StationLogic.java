package net.wtworkshop.station;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.wtworkshop.material.MaterialRecipe;
import net.wtworkshop.material.PartRole;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.part.PartType;
import net.wtworkshop.tool.Modifier;
import net.wtworkshop.tool.ToolData;
import net.wtworkshop.tool.ToolStats;
import net.wtworkshop.tool.ToolType;
import net.wtworkshop.tool.WorkshopTool;
import org.jetbrains.annotations.Nullable;

public final class StationLogic {
   public static final int HEAD = 0;
   public static final int HANDLE = 1;
   public static final int BINDING = 2;
   public static final int TOOL = 3;
   public static final int MATERIAL = 4;
   public static final int SIZE = 5;

   private StationLogic() {
   }

   public static boolean accepts(int slot, ItemStack stack) {
      return switch (slot) {
         case 0 -> {
            if (stack.getItem() instanceof PartItem p && p.type.role == PartRole.HEAD) {
               yield true;
            }

            yield false;
         }
         case 1 -> {
            if (stack.getItem() instanceof PartItem p && p.type.role == PartRole.HANDLE) {
               yield true;
            }

            yield false;
         }
         case 2 -> {
            if (stack.getItem() instanceof PartItem p && p.type.role == PartRole.BINDING) {
               yield true;
            }

            yield false;
         }
         case 3 -> stack.getItem() instanceof WorkshopTool;
         case 4 -> !(stack.getItem() instanceof PartItem) && !(stack.getItem() instanceof WorkshopTool);
         default -> false;
      };
   }

   public static StationLogic.Outcome compute(List<ItemStack> in, Level level) {
      ItemStack tool = in.get(3);
      if (tool.isEmpty()) {
         return assemble(in);
      } else {
         ToolData data = WorkshopTool.data(tool);
         if (data == null) {
            return StationLogic.Outcome.fail("unassembled");
         } else {
            int others = 0;
            int slot = -1;

            for (int i = 0; i < 5; i++) {
               if (i != 3 && !in.get(i).isEmpty()) {
                  others++;
                  slot = i;
               }
            }

            if (others == 0) {
               return StationLogic.Outcome.none();
            } else if (others > 1) {
               return StationLogic.Outcome.fail("one_at_a_time");
            } else {
               WorkshopTool item = (WorkshopTool)tool.getItem();
               return slot == 4 ? material(tool, item.type, data, in.get(4), level) : swapPart(tool, item.type, data, in.get(slot));
            }
         }
      }
   }

   private static StationLogic.Outcome assemble(List<ItemStack> in) {
      if (!in.get(4).isEmpty()) {
         return StationLogic.Outcome.fail("material_needs_tool");
      } else {
         ItemStack head = in.get(0);
         ItemStack handle = in.get(1);
         ItemStack binding = in.get(2);
         if (head.isEmpty() && handle.isEmpty() && binding.isEmpty()) {
            return StationLogic.Outcome.none();
         } else if (!head.isEmpty() && !handle.isEmpty() && !binding.isEmpty()) {
            PartItem h = (PartItem)head.getItem();
            PartItem g = (PartItem)handle.getItem();
            PartItem b = (PartItem)binding.getItem();
            ToolType type = ToolType.forParts(List.of(h.type, g.type, b.type));
            if (type == null) {
               return StationLogic.Outcome.fail("no_tool_for_parts");
            } else {
               ToolMaterial hm = PartItem.material(head);
               ToolMaterial gm = PartItem.material(handle);
               ToolMaterial bm = PartItem.material(binding);
               if (hm != null && gm != null && bm != null) {
                  ItemStack out = WorkshopTool.create(type, ToolData.of(hm.id(), gm.id(), bm.id()));
                  int[] consume = new int[]{1, 1, 1, 0, 0};
                  return new StationLogic.Outcome(out, consume, Component.translatable("station.wt_workshop.assemble", new Object[]{out.getHoverName()}));
               } else {
                  return StationLogic.Outcome.fail("unknown_material");
               }
            }
         } else {
            return StationLogic.Outcome.fail("need_three_parts");
         }
      }
   }

   private static StationLogic.Outcome swapPart(ItemStack tool, ToolType type, ToolData data, ItemStack part) {
      PartItem p = (PartItem)part.getItem();
      PartRole role = p.type.role;
      PartType expected = type.parts().get(role.ordinal());
      if (p.type != expected) {
         return StationLogic.Outcome.fail("wrong_part", expected.displayName());
      } else {
         ToolMaterial m = PartItem.material(part);
         if (m != null && m.supports(role)) {
            if (m.id().equals(data.materialId(role))) {
               return StationLogic.Outcome.fail("same_part");
            } else {
               ItemStack out = tool.copyWithCount(1);
               WorkshopTool.setDataKeepWear(out, data.withMaterial(role, m.id()));
               return StationLogic.Outcome.of(
                  out, role.ordinal(), 1, Component.translatable("station.wt_workshop.replace", new Object[]{expected.displayName(), m.displayName()})
               );
            }
         } else {
            return StationLogic.Outcome.fail("unknown_material");
         }
      }
   }

   private static StationLogic.Outcome material(ItemStack tool, ToolType type, ToolData data, ItemStack input, Level level) {
      var expansion=net.wtworkshop.tool.SlotExpansion.of(input);if(expansion!=null)return expand(tool,data,expansion);
      if (!input.is(Items.SPONGE) && !input.is(Items.WET_SPONGE)) {
         Optional<MaterialRecipe> mat = MaterialRecipe.find(level, input);
         String headId = data.materialId(PartRole.HEAD);
         if (mat.isPresent() && mat.get().material().equals(headId) && tool.isDamaged()) {
            MaterialRecipe r = mat.get();
            int perUnit = Math.max(1, tool.getMaxDamage() / 4);
            int unitsNeeded = (tool.getDamageValue() + perUnit - 1) / perUnit;
            int units = Math.min(unitsNeeded, r.units(input.getCount()));
            if (units <= 0) {
               return StationLogic.Outcome.fail("not_enough_material", r.itemsFor(1));
            } else {
               ItemStack out = tool.copyWithCount(1);
               out.setDamageValue(Math.max(0, out.getDamageValue() - units * perUnit));
               return StationLogic.Outcome.of(out, 4, r.itemsFor(units), Component.translatable("station.wt_workshop.repair", new Object[]{units * 25}));
            }
         } else {
            Modifier modifier = Modifier.forIngredient(input);
            if (modifier != null) {
               return addModifier(tool, type, data, modifier, input);
            } else {
               if(net.wtworkshop.api.AddonModifiers.matching(input).size()>1)return Outcome.fail("addon_ambiguous");
               var addon=net.wtworkshop.api.AddonModifiers.forIngredient(input);
               if(addon!=null)return addAddon(tool,type,data,addon,input);
               return mat.isPresent() && mat.get().material().equals(headId)
                  ? StationLogic.Outcome.fail("not_damaged")
                  : StationLogic.Outcome.fail("unknown_ingredient");
            }
         }
      } else if (data.modifiers().isEmpty() && data.progress().isEmpty()) {
         return StationLogic.Outcome.fail("nothing_to_remove");
      } else {
         ItemStack out = tool.copyWithCount(1);
         ToolData updated = data.withLastModifierRemoved();
         WorkshopTool.setDataKeepWear(out, updated);
         return StationLogic.Outcome.of(out, 4, 1, Component.translatable("station.wt_workshop.remove_modifier"));
      }
   }

   private static Outcome expand(ItemStack tool,ToolData data,net.wtworkshop.tool.SlotExpansion expansion){
      if(expansion.tier()>net.wtworkshop.WorkshopConfig.expansionTierLimit())return Outcome.fail("expansion_disabled");
      if(data.expansions().contains(expansion.id()))return Outcome.fail("expansion_used");
      if(expansion.tier()!=data.expansionTier()+1)return Outcome.fail("expansion_order",data.expansionTier()+1);
      var out=tool.copyWithCount(1);WorkshopTool.setDataKeepWear(out,data.withExpansion(expansion.id()));
      return Outcome.of(out,MATERIAL,1,Component.translatable("station.wt_workshop.expanded",expansion.displayName()));
   }

   private static StationLogic.Outcome addModifier(ItemStack tool, ToolType type, ToolData data, Modifier m, ItemStack input) {
      if(m.maximumLevel()==0)return Outcome.fail("modifier_disabled");
      if (!m.allowedOn(type)) {
         return StationLogic.Outcome.fail("modifier_not_allowed", m.displayName(1));
      } else {
         int current = data.modifierLevel(m);
         if (current >= m.maximumLevel()) {
            return StationLogic.Outcome.fail("modifier_maxed", m.displayName(current));
         } else {
            Modifier required = m.requires();
            if (required != null && data.modifierLevel(required) == 0) {
               return StationLogic.Outcome.fail("modifier_requires", required.displayName(1));
            } else {
               for(var other:net.wtworkshop.api.AddonModifiers.values())if(data.modifierLevel(other.id())>0&&other.conflicts().contains(m.id()))return Outcome.fail("modifier_conflict",other.displayName(data.modifierLevel(other.id())));
               for (Modifier other : Modifier.values()) {
                  if (other != m && m.conflictsWith(other) && data.modifierLevel(other) > 0) {
                     return StationLogic.Outcome.fail("modifier_conflict", other.displayName(1));
                  }
               }

               ToolStats stats = ToolStats.of(type, data);
               int free = m.slot == Modifier.Slot.UPGRADE ? stats.freeUpgrades() : stats.freeAbilities();
               if (free <= 0) {
                  return StationLogic.Outcome.fail(m.slot == Modifier.Slot.UPGRADE ? "no_upgrade_slots" : "no_ability_slots");
               } else {
                  int value = m.valueOf(input);
                  int have = data.progressOf(m);
                  int missing = m.perLevel - have;
                  int take = Math.min(input.getCount(), (missing + value - 1) / value);
                  int points = have + take * value;
                  ItemStack out = tool.copyWithCount(1);
                  if (points >= m.perLevel) {
                     WorkshopTool.setDataKeepWear(out, data.withModifierAdded(m));
                     return StationLogic.Outcome.of(
                        out, 4, take, Component.translatable("station.wt_workshop.add_modifier", new Object[]{m.displayName(current + 1)})
                     );
                  } else {
                     WorkshopTool.setDataKeepWear(out, data.withProgress(m, points));
                     return StationLogic.Outcome.of(
                        out,
                        4,
                        take,
                        Component.translatable("station.wt_workshop.progress_modifier", new Object[]{m.displayName(current + 1), points, m.perLevel})
                     );
                  }
               }
            }
         }
      }
   }

   private static Outcome addAddon(ItemStack tool,ToolType type,ToolData data,net.wtworkshop.api.AddonModifier m,ItemStack input){
      if(m.maxLevel()==0)return Outcome.fail("modifier_disabled");
      if(!m.allowed().test(type))return Outcome.fail("modifier_not_allowed",m.displayName(1));
      int current=data.modifierLevel(m.id());if(current>=m.maxLevel())return Outcome.fail("modifier_maxed",m.displayName(current));
      for(String id:data.modifiers()){var other=net.wtworkshop.api.AddonModifiers.get(id);if(m.conflicts().contains(id)||(other!=null&&other.conflicts().contains(m.id())))return Outcome.fail("modifier_conflict",id);}
      var stats=ToolStats.of(type,data);if((m.slot()==Modifier.Slot.UPGRADE?stats.freeUpgrades():stats.freeAbilities())<=0)return Outcome.fail(m.slot()==Modifier.Slot.UPGRADE?"no_upgrade_slots":"no_ability_slots");
      int have=data.progressOf(m.id()),take=Math.min(input.getCount(),Math.max(0,m.pointsPerLevel()-have));if(take==0)return Outcome.fail("modifier_maxed",m.displayName(current));
      int points=have+take;var updated=data.withAddonSlot(m.id(),m.slot());boolean completed=points>=m.pointsPerLevel();updated=completed?updated.withModifierAdded(m.id()):updated.withProgress(m.id(),points);
      var out=tool.copyWithCount(1);WorkshopTool.setDataKeepWear(out,updated);
      return Outcome.of(out,MATERIAL,take,completed?Component.translatable("station.wt_workshop.add_modifier",m.displayName(current+1)):Component.translatable("station.wt_workshop.progress_modifier",m.displayName(current+1),points,m.pointsPerLevel()));
   }

   public static record Outcome(ItemStack result, int[] consume, @Nullable Component message) {
      static StationLogic.Outcome fail(String key, Object... args) {
         return new StationLogic.Outcome(ItemStack.EMPTY, new int[5], Component.translatable("station.wt_workshop.error." + key, args));
      }

      static StationLogic.Outcome none() {
         return new StationLogic.Outcome(ItemStack.EMPTY, new int[5], null);
      }

      static StationLogic.Outcome of(ItemStack result, int slot, int count, Component message) {
         int[] consume = new int[5];
         consume[TOOL] = 1;
         consume[slot] = count;
         return new StationLogic.Outcome(result, consume, message);
      }

      public boolean success() {
         return !this.result.isEmpty();
      }
   }
}
