package net.wtworkshop.tool;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.wtworkshop.Registration;
import net.wtworkshop.WorkshopConfig;
import net.wtworkshop.material.PartRole;
import net.wtworkshop.material.ToolMaterial;
import net.wtworkshop.material.Trait;
import net.wtworkshop.part.PartItem;
import net.wtworkshop.util.Text;
import org.jetbrains.annotations.Nullable;

public class WorkshopTool extends Item {
   public final ToolType type;
   private static final List<ResourceKey<Enchantment>> VIRTUAL = List.of(
      Enchantments.FORTUNE, Enchantments.LOOTING, Enchantments.SILK_TOUCH, Enchantments.FIRE_ASPECT, Enchantments.KNOCKBACK
   );

   public WorkshopTool(ToolType type) {
      super(new Properties().durability(1));
      this.type = type;
   }

   public static ItemStack create(ToolType type, ToolData data) {
      ItemStack stack = new ItemStack((ItemLike)Registration.TOOLS.get(type).get());
      stack.set((DataComponentType)Registration.TOOL_DATA.get(), data);
      return stack;
   }

   @Nullable
   public static ToolData data(ItemStack stack) {
      ToolData d = (ToolData)stack.get((DataComponentType)Registration.TOOL_DATA.get());
      return d != null && d.valid() ? d : null;
   }

   @Nullable
   public static ToolStats stats(ItemStack stack) {
      ToolData d = data(stack);
      return d != null && stack.getItem() instanceof WorkshopTool tool ? ToolStats.of(tool.type, d) : null;
   }

   public static boolean usable(ItemStack stack) {
      return data(stack) != null && stack.getDamageValue() < stack.getMaxDamage() - 1;
   }

   public static void setDataKeepWear(ItemStack stack, ToolData data) {
      double worn = stack.getMaxDamage() > 0 ? (double)stack.getDamageValue() / (double)stack.getMaxDamage() : 0.0;
      stack.set((DataComponentType)Registration.TOOL_DATA.get(), data);
      int max = stack.getMaxDamage();
      stack.setDamageValue(Math.min(max - 1, (int)Math.ceil(worn * (double)max)));
   }

   public int getMaxDamage(ItemStack stack) {
      ToolStats s = stats(stack);
      return s == null ? 1 : s.durability();
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
      ToolData d = data(stack);
      if (d == null) {
         return 0;
      } else {
         if (entity != null && amount > 0) {
            float spare = ToolStats.of(this.type, d).spareChance(d);
            int kept = 0;

            for (int i = 0; i < amount; i++) {
               if (entity.getRandom().nextFloat() < spare) {
                  kept++;
               }
            }

            amount -= kept;
         }

         return Math.max(0, Math.min(amount, stack.getMaxDamage() - 1 - stack.getDamageValue()));
      }
   }

   public boolean isRepairable(ItemStack stack) {
      return false;
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
      return false;
   }

   private boolean effectiveOn(BlockState state) {
      return state.is(this.type.mineable) || this.type == ToolType.SWORD && state.is(Blocks.COBWEB);
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      ToolStats s = stats(stack);
      if (s == null || !usable(stack) || !this.effectiveOn(state)) {
         return 1.0F;
      } else if (state.is(s.tier().getIncorrectBlocksForDrops())) {
         return 1.0F;
      } else if (this.type == ToolType.SWORD) {
         return state.is(Blocks.COBWEB) ? 15.0F : 1.5F;
      } else {
         return Math.max(1.0F, s.speed());
      }
   }

   public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
      ToolStats s = stats(stack);
      return s != null && usable(stack) && this.effectiveOn(state) && !state.is(s.tier().getIncorrectBlocksForDrops());
   }

   public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
      if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F && data(stack) != null) {
         boolean correct = this.isCorrectToolForDrops(stack, state);
         stack.hurtAndBreak(this.type == ToolType.SWORD ? 2 : 1, miner, EquipmentSlot.MAINHAND);
         if (correct && miner instanceof Player player) {
            ToolLeveling.addXp(stack, player, 1);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
               ToolEvents.afterBlockBreak(serverPlayer, pos, state);
            }
         }
      }

      return true;
   }

   public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
      return usable(stack) && this.type.abilities.contains(ability);
   }

   public InteractionResult useOn(UseOnContext context) {
      if (!usable(context.getItemInHand())) {
         return InteractionResult.PASS;
      } else {
         return switch (this.type) {
            case AXE, BROAD_AXE -> Items.IRON_AXE.useOn(context);
            case SHOVEL, EXCAVATOR -> Items.IRON_SHOVEL.useOn(context);
            case HOE -> Items.IRON_HOE.useOn(context);
            default -> InteractionResult.PASS;
         };
      }
   }

   public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
      ToolStats s = stats(stack);
      if (s != null && usable(stack)) {
         float attack = s.attack();
         int jagged = s.trait(Trait.JAGGED);
         if (jagged > 0 && stack.getMaxDamage() > 0) {
            attack += (float)jagged * 1.5F * (float)stack.getDamageValue() / (float)stack.getMaxDamage();
         }

         return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)attack, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(
               Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)s.attackSpeed(), Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
            )
            .build();
      } else {
         return ItemAttributeModifiers.EMPTY;
      }
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      return true;
   }

   public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      ToolStats s = stats(stack);
      if (s != null && !attacker.level().isClientSide) {
         boolean wasUsable = usable(stack);
         stack.hurtAndBreak(this.type.isMelee() ? 1 : 2, attacker, EquipmentSlot.MAINHAND);
         if (wasUsable) {
            if (s.trait(Trait.BLAZING) > 0) {
               target.igniteForSeconds((float)(3 * s.trait(Trait.BLAZING)));
            }

            if (this.type.isMelee() && attacker instanceof Player player) {
               ToolLeveling.addXp(stack, player, 1);
            }
         }
      }
   }

   public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
      return (this.type == ToolType.AXE || this.type == ToolType.BROAD_AXE) && usable(stack);
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
      return false;
   }

   public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
      return false;
   }

   private int virtualLevel(ToolData d, ResourceKey<Enchantment> key) {
      if (key == Enchantments.FORTUNE) {
         return this.type.isMelee() ? 0 : d.modifierLevel(Modifier.LUCK);
      } else if (key == Enchantments.LOOTING) {
         return d.modifierLevel(Modifier.LUCK);
      } else if (key == Enchantments.SILK_TOUCH) {
         return d.modifierLevel(Modifier.SILKY);
      } else if (key == Enchantments.FIRE_ASPECT) {
         return d.modifierLevel(Modifier.FIERY);
      } else {
         return key == Enchantments.KNOCKBACK ? d.modifierLevel(Modifier.KNOCKBACK) : 0;
      }
   }

   public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
      ToolData d = data(stack);
      if (d != null && usable(stack)) {
         for (ResourceKey<Enchantment> key : VIRTUAL) {
            if (enchantment.is(key)) {
               return this.virtualLevel(d, key);
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
      ToolData d = data(stack);
      if (d != null && usable(stack)) {
         Mutable out = new Mutable(ItemEnchantments.EMPTY);

         for (ResourceKey<Enchantment> key : VIRTUAL) {
            int level = this.virtualLevel(d, key);
            if (level > 0) {
               lookup.get(key).ifPresent(h -> out.set(h, level));
            }
         }

         return out.toImmutable();
      } else {
         return ItemEnchantments.EMPTY;
      }
   }

   public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
      ToolData d = data(stack);
      return d != null && source.is(DamageTypeTags.IS_FIRE) && ToolStats.of(this.type, d).fireproof(d) ? false : super.canBeHurtBy(stack, source);
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return slotChanged || !oldStack.is(newStack.getItem());
   }

   public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
      return !newStack.is(oldStack.getItem());
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide) {
         if (!stack.has((DataComponentType)Registration.TOOL_DATA.get())) {
            this.migrateLegacy(stack);
         } else {
            ToolStats s = stats(stack);
            boolean fireproof = s != null && s.fireproof(data(stack));
            if (fireproof != stack.has(DataComponents.FIRE_RESISTANT)) {
               if (fireproof) {
                  stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
               } else {
                  stack.remove(DataComponents.FIRE_RESISTANT);
               }
            }

            if (s != null
               && s.trait(Trait.ECOLOGICAL) > 0
               && stack.isDamaged()
               && level.getGameTime() % 200L == (long)slot * 7L % 200L
               && level.random.nextInt(3) < s.trait(Trait.ECOLOGICAL)) {
               stack.setDamageValue(stack.getDamageValue() - 1);
            }
         }
      }
   }

   public void migrateLegacy(ItemStack stack) {
      CustomData custom = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (custom != null) {
         CompoundTag t = custom.copyTag();
         if (t.getBoolean("assembled")) {
            int oldMax = stack.has(DataComponents.MAX_DAMAGE) ? Math.max(1, (Integer)stack.getOrDefault(DataComponents.MAX_DAMAGE, 1)) : 1;
            double worn = (double)((Integer)stack.getOrDefault(DataComponents.DAMAGE, 0)).intValue() / (double)oldMax;
            ToolData d = ToolData.of(t.getString("head"), t.getString("handle"), t.getString("binding"));

            for (int i = 0; i < Math.min(5, t.getInt("redstone")); i++) {
               d = d.withModifierAdded(Modifier.HASTE);
            }

            if (t.getInt("diamond") > 0) {
               d = d.withModifierAdded(Modifier.DIAMOND);
            }

            for (int i = 0; i < Math.min(3, t.getInt("lapis")); i++) {
               d = d.withModifierAdded(Modifier.LUCK);
            }

            if (t.getInt("silk") > 0) {
               d = d.withModifierAdded(Modifier.SILKY);
            }

            d = d.withXp(t.getInt("level"), t.getLong("xp"));

            for (String key : List.of("assembled", "head", "handle", "binding", "redstone", "diamond", "lapis", "silk", "level", "xp")) {
               t.remove(key);
            }

            if (t.isEmpty()) {
               stack.remove(DataComponents.CUSTOM_DATA);
            } else {
               stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
            }

            stack.remove(DataComponents.ENCHANTMENTS);
            stack.remove(DataComponents.MAX_DAMAGE);
            stack.set((DataComponentType)Registration.TOOL_DATA.get(), d);
            int max = stack.getMaxDamage();
            stack.setDamageValue(Math.min(max - 1, (int)Math.ceil(worn * (double)max)));
         }
      }
   }

   public Component getName(ItemStack stack) {
      ToolData d = data(stack);
      ToolMaterial head = d == null ? null : d.material(PartRole.HEAD);
      return (Component)(head == null
         ? super.getName(stack)
         : Component.translatable("item.wt_workshop.tool_with_material", new Object[]{head.displayName(), super.getName(stack)}));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
      ToolData d = data(stack);
      if (d == null) {
         lines.add(Component.translatable("tooltip.wt_workshop.tool.unassembled").withStyle(ChatFormatting.GRAY));
      } else {
         ToolStats s = ToolStats.of(this.type, d);
         if (!usable(stack)) {
            lines.add(Component.translatable("tooltip.wt_workshop.tool.worn").withStyle(ChatFormatting.RED));
         }

         int maxLevel = WorkshopConfig.maxLevel();
         Component xp = d.level() >= maxLevel
            ? Component.translatable("tooltip.wt_workshop.tool.max_level")
            : Component.literal(d.xp() + " / " + ToolLeveling.xpNeeded(d.level()));
         lines.add(Component.translatable("tooltip.wt_workshop.tool.level", new Object[]{d.level(), xp}).withStyle(ChatFormatting.AQUA));
         lines.add(Text.stat("durability", stack.getMaxDamage() - stack.getDamageValue() + " / " + stack.getMaxDamage()));
         if (this.type != ToolType.SWORD) {
            lines.add(Text.stat("mining_speed", Text.number((double)s.speed())));
         }

         lines.add(Text.stat("tier", PartItem.tierName(s.tier())));
         lines.add(
            Component.translatable("tooltip.wt_workshop.tool.slots", new Object[]{s.freeUpgrades(), s.upgradeSlots(), s.freeAbilities(), s.abilitySlots()})
               .withStyle(ChatFormatting.GOLD)
         );
         lines.add(Component.translatable("tooltip.wt_workshop.expansion_count",d.expansionTier()).withStyle(ChatFormatting.GOLD));
         boolean details = Text.shiftDown();

         for (Modifier m : Modifier.values()) {
            int level = d.modifierLevel(m);
            int progress = d.progressOf(m);
            if (level > 0) {
               lines.add(m.displayName(level));
               if (details) {
                  lines.add(Text.indent(m.description()));
               }
            }

            if (progress > 0) {
               lines.add(
                  Component.translatable("tooltip.wt_workshop.tool.progress", new Object[]{m.displayName(level + 1), progress, m.perLevel})
                     .withStyle(ChatFormatting.DARK_GRAY)
               );
            }
         }

         lines.addAll(net.wtworkshop.api.AddonModifiers.tooltip(d,details));
         s.traits().forEach((t, l) -> {
            lines.add(t.displayName(l));
            if (details) {
               lines.add(Text.indent(t.description()));
            }
         });
         if (details) {
            for (PartRole role : PartRole.values()) {
               ToolMaterial m = d.material(role);
               if (m != null) {
                  lines.add(
                     Component.translatable("tooltip.wt_workshop.tool.part", new Object[]{this.type.parts().get(role.ordinal()).displayName(), m.displayName()})
                        .withStyle(ChatFormatting.DARK_GRAY)
                  );
               }
            }
         } else {
            lines.add(Text.holdShift());
         }
      }
   }
}
