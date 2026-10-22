package net.wtworkshop.builder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class PartBuilderBlock extends Block {
   public PartBuilderBlock(Properties properties) {
      super(properties.noOcclusion());
   }


   private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
      Block.box(0,12,0,16,14,16),
      Block.box(1,0,1,4,12,4), Block.box(12,0,1,15,12,4),
      Block.box(1,0,12,4,12,15), Block.box(12,0,12,15,12,15),
      Block.box(2,10,2,14,12,4),Block.box(2,10,12,14,12,14),
      Block.box(2,10,2,4,12,14),Block.box(12,10,2,14,12,14),
      Block.box(2,3,2,14,4,4),Block.box(2,3,12,14,4,14),
      Block.box(6,14,4,14,15,12)
   );
   @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return SHAPE;}

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide) {
         player.openMenu(
            new SimpleMenuProvider(
               (id, inventory, p) -> new PartBuilderMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
               Component.translatable("block.wt_workshop.part_builder")
            )
         );
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }
}
