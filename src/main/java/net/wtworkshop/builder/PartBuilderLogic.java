package net.wtworkshop.builder;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.wtworkshop.Registration;
import net.wtworkshop.material.MaterialRecipe;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.part.PartType;
import net.wtworkshop.part.PatternItem;
import org.jetbrains.annotations.Nullable;

public final class PartBuilderLogic {
   private PartBuilderLogic() {
   }

   public static PartBuilderLogic.Outcome compute(ItemStack pattern, ItemStack material, int selected, Level level) {
      if (pattern.isEmpty()) {
         return PartBuilderLogic.Outcome.fail("need_pattern");
      } else if (pattern.is((Item)Registration.BLANK_PATTERN.get())) {
         if (selected >= 0 && selected < PartType.values().length) {
            PartType type = PartType.values()[selected];
            return new PartBuilderLogic.Outcome(
               new ItemStack((ItemLike)Registration.PATTERNS.get(type).get()),
               1,
               0,
               Component.translatable("builder.wt_workshop.carve", new Object[]{type.displayName()})
            );
         } else {
            return PartBuilderLogic.Outcome.fail("choose_pattern");
         }
      } else if (pattern.getItem() instanceof PatternItem p) {
         if (material.isEmpty()) {
            return PartBuilderLogic.Outcome.fail("need_material");
         } else {
            Optional<MaterialRecipe> recipe = MaterialRecipe.find(level, material);
            if (!recipe.isEmpty() && recipe.get().toolMaterial() != null) {
               MaterialRecipe r = recipe.get();
               ToolMaterial m = r.toolMaterial();
               if (m.castable()) {
                  return PartBuilderLogic.Outcome.fail("castable", m.displayName());
               } else if (!m.supports(p.type.role)) {
                  return PartBuilderLogic.Outcome.fail("wrong_role", m.displayName(), p.type.displayName());
               } else {
                  int items = r.itemsFor(p.type.cost);
                  return material.getCount() < items
                     ? PartBuilderLogic.Outcome.fail("not_enough", items, material.getHoverName())
                     : new PartBuilderLogic.Outcome(
                        PartItem.create(p.type, m), 0, items, Component.translatable("builder.wt_workshop.make", new Object[]{items, material.getHoverName()})
                     );
               }
            } else {
               return PartBuilderLogic.Outcome.fail("not_a_material");
            }
         }
      } else {
         return PartBuilderLogic.Outcome.fail("need_pattern");
      }
   }

   public static record Outcome(ItemStack result, int patternCost, int materialCost, @Nullable Component message) {
      static PartBuilderLogic.Outcome fail(String key, Object... args) {
         return new PartBuilderLogic.Outcome(ItemStack.EMPTY, 0, 0, Component.translatable("builder.wt_workshop.error." + key, args));
      }

      public boolean success() {
         return !this.result.isEmpty();
      }
   }
}
