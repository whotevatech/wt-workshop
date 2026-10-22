package net.wtworkshop.tool;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.wtworkshop.part.PartType;
import org.jetbrains.annotations.Nullable;

public enum ToolType {
   PICKAXE(
      PartType.PICKAXE_HEAD,
      PartType.HANDLE,
      PartType.BINDING,
      BlockTags.MINEABLE_WITH_PICKAXE,
      1.0F,
      1.0F,
      1.0F,
      1.0F,
      -2.8F,
      ToolType.Area.NONE,
      ItemAbilities.DEFAULT_PICKAXE_ACTIONS
   ),
   AXE(
      PartType.AXE_HEAD,
      PartType.HANDLE,
      PartType.BINDING,
      BlockTags.MINEABLE_WITH_AXE,
      1.0F,
      1.0F,
      5.5F,
      1.0F,
      -3.0F,
      ToolType.Area.NONE,
      ItemAbilities.DEFAULT_AXE_ACTIONS
   ),
   SHOVEL(
      PartType.SHOVEL_HEAD,
      PartType.HANDLE,
      PartType.BINDING,
      BlockTags.MINEABLE_WITH_SHOVEL,
      1.0F,
      1.0F,
      1.5F,
      1.0F,
      -3.0F,
      ToolType.Area.NONE,
      ItemAbilities.DEFAULT_SHOVEL_ACTIONS
   ),
   HOE(
      PartType.HOE_HEAD,
      PartType.HANDLE,
      PartType.BINDING,
      BlockTags.MINEABLE_WITH_HOE,
      1.0F,
      1.0F,
      0.0F,
      0.5F,
      -1.0F,
      ToolType.Area.NONE,
      ItemAbilities.DEFAULT_HOE_ACTIONS
   ),
   SWORD(
      PartType.SWORD_BLADE,
      PartType.HANDLE,
      PartType.GUARD,
      BlockTags.SWORD_EFFICIENT,
      1.1F,
      1.0F,
      3.0F,
      1.0F,
      -2.4F,
      ToolType.Area.NONE,
      ItemAbilities.DEFAULT_SWORD_ACTIONS
   ),
   HAMMER(
      PartType.HAMMER_HEAD,
      PartType.TOUGH_HANDLE,
      PartType.TOUGH_BINDING,
      BlockTags.MINEABLE_WITH_PICKAXE,
      4.0F,
      0.5F,
      5.0F,
      1.25F,
      -3.4F,
      ToolType.Area.PLANE,
      ItemAbilities.DEFAULT_PICKAXE_ACTIONS
   ),
   EXCAVATOR(
      PartType.EXCAVATOR_HEAD,
      PartType.TOUGH_HANDLE,
      PartType.TOUGH_BINDING,
      BlockTags.MINEABLE_WITH_SHOVEL,
      3.75F,
      0.55F,
      3.0F,
      1.0F,
      -3.2F,
      ToolType.Area.PLANE,
      ItemAbilities.DEFAULT_SHOVEL_ACTIONS
   ),
   BROAD_AXE(
      PartType.BROAD_AXE_HEAD,
      PartType.TOUGH_HANDLE,
      PartType.TOUGH_BINDING,
      BlockTags.MINEABLE_WITH_AXE,
      4.25F,
      0.5F,
      7.0F,
      1.25F,
      -3.4F,
      ToolType.Area.TREE,
      ItemAbilities.DEFAULT_AXE_ACTIONS
   );

   public final PartType head;
   public final PartType handle;
   public final PartType binding;
   public final TagKey<Block> mineable;
   public final float durabilityScale;
   public final float speedScale;
   public final float baseAttack;
   public final float attackScale;
   public final float attackSpeed;
   public final ToolType.Area area;
   public final Set<ItemAbility> abilities;

   private ToolType(
      PartType head,
      PartType handle,
      PartType binding,
      TagKey<Block> mineable,
      float durabilityScale,
      float speedScale,
      float baseAttack,
      float attackScale,
      float attackSpeed,
      ToolType.Area area,
      Set<ItemAbility> abilities
   ) {
      this.head = head;
      this.handle = handle;
      this.binding = binding;
      this.mineable = mineable;
      this.durabilityScale = durabilityScale;
      this.speedScale = speedScale;
      this.baseAttack = baseAttack;
      this.attackScale = attackScale;
      this.attackSpeed = attackSpeed;
      this.area = area;
      this.abilities = abilities;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public List<PartType> parts() {
      return List.of(this.head, this.handle, this.binding);
   }

   public boolean isMelee() {
      return this == SWORD;
   }

   public boolean isHeavy() {
      return this.area != ToolType.Area.NONE;
   }

   @Nullable
   public static ToolType forParts(List<PartType> parts) {
      if (parts.size() != 3) {
         return null;
      } else {
         for (ToolType t : values()) {
            if (parts.contains(t.head) && parts.contains(t.handle) && parts.contains(t.binding)) {
               return t;
            }
         }

         return null;
      }
   }

   public static enum Area {
      NONE,
      PLANE,
      TREE;
   }
}
