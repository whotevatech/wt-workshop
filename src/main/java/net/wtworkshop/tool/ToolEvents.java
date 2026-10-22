package net.wtworkshop.tool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.wtworkshop.WorkshopConfig;
import net.wtworkshop.material.Trait;

@EventBusSubscriber(
   modid = "wt_workshop"
)
public final class ToolEvents {
   private static boolean breakingArea;

   private ToolEvents() {
   }

   /** Called only after the original block was successfully removed. */
   public static void afterBlockBreak(ServerPlayer player, BlockPos origin, BlockState center) {
      if (!breakingArea && !player.isShiftKeyDown()) {
         ItemStack stack = player.getMainHandItem();
         if (stack.getItem() instanceof WorkshopTool tool && tool.type.isHeavy() && WorkshopTool.usable(stack)) {
            if (!tool.isCorrectToolForDrops(stack, center) && !center.is(tool.type.mineable)) {
               return;
            }

            Level level = player.level();
            ToolData data = WorkshopTool.data(stack);
            int radius = 1 + data.modifierLevel(Modifier.EXPANDED);
            List<BlockPos> targets = tool.type.area == ToolType.Area.TREE && center.is(BlockTags.LOGS)
               ? treeBlocks(level, origin, WorkshopConfig.maxTreeLogs())
               : planeBlocks(player, origin, radius);
            float centerHardness = center.getDestroySpeed(level, origin);
            breakingArea = true;

            try {
               for (BlockPos pos : targets) {
                  if (!pos.equals(origin) && WorkshopTool.usable(player.getMainHandItem())) {
                     BlockState state = level.getBlockState(pos);
                     float hardness = state.getDestroySpeed(level, pos);
                     if (!state.isAir() && !(hardness < 0.0F) && !(hardness > centerHardness + 2.0F) && state.is(tool.type.mineable)) {
                        player.gameMode.destroyBlock(pos);
                     }
                  }
               }
            } finally {
               breakingArea = false;
            }

            return;
         }
      }
   }

   private static List<BlockPos> planeBlocks(Player player, BlockPos center, int radius) {
      Direction var10000;
      label35: {
         double reach = player.blockInteractionRange();
         Vec3 eye = player.getEyePosition();
         Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
         HitResult hit = player.level().clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
         if (hit instanceof BlockHitResult bhr && hit.getType() == Type.BLOCK) {
            var10000 = bhr.getDirection();
            break label35;
         }

         var10000 = Direction.UP;
      }

      Direction face = var10000;
      List<BlockPos> out = new ArrayList<>();

      for (int a = -radius; a <= radius; a++) {
         for (int b = -radius; b <= radius; b++) {
            out.add(switch (face.getAxis()) {
               case X -> center.offset(0, a, b);
               case Y -> center.offset(a, 0, b);
               case Z -> center.offset(a, b, 0);
               default -> throw new MatchException(null, null);
            });
         }
      }

      return out;
   }

   private static List<BlockPos> treeBlocks(Level level, BlockPos start, int limit) {
      List<BlockPos> out = new ArrayList<>();
      Set<BlockPos> seen = new HashSet<>();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      queue.add(start);
      seen.add(start);

      while (!queue.isEmpty() && out.size() < limit) {
         BlockPos pos = queue.poll();
         out.add(pos);

         for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
               for (int dz = -1; dz <= 1; dz++) {
                  BlockPos next = pos.offset(dx, dy, dz);
                  if (Math.abs(next.getX() - start.getX()) <= 12
                     && Math.abs(next.getZ() - start.getZ()) <= 12
                     && seen.add(next)
                     && level.getBlockState(next).is(BlockTags.LOGS)) {
                     queue.add(next);
                  }
               }
            }
         }
      }

      return out;
   }

   @SubscribeEvent
   public static void onBlockDrops(BlockDropsEvent event) {
      if (event.getBreaker() instanceof Player player) {
         ItemStack tool = event.getTool();
         ToolData data = WorkshopTool.data(tool);
         if (data != null && tool.getItem() instanceof WorkshopTool wt) {
            ToolStats var10 = ToolStats.of(wt.type, data);
            if (data.modifierLevel(Modifier.AUTOSMELT) > 0) {
               for (ItemEntity entity : event.getDrops()) {
                  ItemStack drop = entity.getItem();
                  event.getLevel()
                     .getRecipeManager()
                     .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(drop), event.getLevel())
                     .map(r -> ((SmeltingRecipe)r.value()).getResultItem(event.getLevel().registryAccess()))
                     .filter(r -> !r.isEmpty())
                     .ifPresent(r -> entity.setItem(r.copyWithCount(r.getCount() * drop.getCount())));
               }
            }

            if (var10.magnetic(data)) {
               pullToPlayer(event.getDrops(), player);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDrops(LivingDropsEvent event) {
      if (event.getSource().getEntity() instanceof Player player) {
         ItemStack tool = player.getMainHandItem();
         ToolData data = WorkshopTool.data(tool);
         if (data != null && tool.getItem() instanceof WorkshopTool wt && ToolStats.of(wt.type, data).magnetic(data)) {
            pullToPlayer(event.getDrops(), player);
         }
      }
   }

   private static void pullToPlayer(Iterable<ItemEntity> drops, Player player) {
      for (ItemEntity entity : drops) {
         entity.setPos(player.getX(), player.getY() + 0.25, player.getZ());
         entity.setDeltaMovement(Vec3.ZERO);
         entity.setNoPickUpDelay();
      }
   }

   @SubscribeEvent
   public static void onBreakSpeed(BreakSpeed event) {
      ItemStack stack = event.getEntity().getMainHandItem();
      ToolStats stats = WorkshopTool.stats(stack);
      if (stats != null && WorkshopTool.usable(stack)) {
         float speed = event.getNewSpeed();
         int stonebound = stats.trait(Trait.STONEBOUND);
         if (stonebound > 0 && stack.getMaxDamage() > 0) {
            speed *= 1.0F + 0.5F * (float)stonebound * (float)stack.getDamageValue() / (float)stack.getMaxDamage();
         }

         int dwarven = stats.trait(Trait.DWARVEN);
         if (dwarven > 0) {
            double y = (double)event.getPosition().<Integer>map(Vec3i::getY).orElse((int)event.getEntity().getY()).intValue();
            if (y < 64.0) {
               speed *= 1.0F + (float)dwarven * (float)Math.min(1.0, (64.0 - y) / 128.0);
            }
         }

         if (stats.trait(Trait.CRUMBLING) > 0 && !event.getState().requiresCorrectToolForDrops()) {
            speed *= 1.0F + (float)stats.trait(Trait.CRUMBLING);
         }

         event.setNewSpeed(speed);
      }
   }

   @SubscribeEvent
   public static void onPickupXp(PickupXp event) {
      Player player = event.getEntity();
      ExperienceOrb orb = event.getOrb();

      for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
         if (orb.value <= 0) {
            return;
         }

         ToolData data = WorkshopTool.data(stack);
         if (data != null && data.modifierLevel(Modifier.MENDING) > 0 && stack.isDamaged()) {
            int repair = Math.min(orb.value * 2, stack.getDamageValue());
            stack.setDamageValue(stack.getDamageValue() - repair);
            orb.value -= (repair + 1) / 2;
         }
      }
   }

   @SubscribeEvent
   public static void onAnvil(AnvilUpdateEvent event) {
      if (event.getLeft().getItem() instanceof WorkshopTool && !event.getRight().isEmpty()) {
         event.setCanceled(true);
      }
   }
}
