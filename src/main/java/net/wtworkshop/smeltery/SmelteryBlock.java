package net.wtworkshop.smeltery;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.wtworkshop.Registration;
import org.jetbrains.annotations.Nullable;

public class SmelteryBlock extends HorizontalDirectionalBlock implements EntityBlock {
   public static final MapCodec<SmelteryBlock> CODEC = simpleCodec(SmelteryBlock::new);
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   public SmelteryBlock(Properties properties) {
      super(properties.noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(LIT, false));
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
      Block.box(0,0,0,16,10,16),Block.box(0,10,0,16,16,3),Block.box(0,10,13,16,16,16),
      Block.box(0,10,3,3,16,13),Block.box(13,10,3,16,16,13));
   @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return SHAPE;}

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, LIT});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new SmelteryBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return !level.isClientSide && type == Registration.SMELTERY_BE.get()
         ? (world, pos, blockState, entity) -> SmelteryBlockEntity.serverTick(world, pos, blockState, (SmelteryBlockEntity) entity)
         : null;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryBlockEntity be) {
         player.openMenu(be);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryBlockEntity be) {
         be.dropContents(level, pos);
      }

      super.onRemove(state, level, pos, newState, moved);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if ((Boolean)state.getValue(LIT)) {
         double x = (double)pos.getX() + 0.5;
         double y = (double)pos.getY();
         double z = (double)pos.getZ() + 0.5;
         if (random.nextDouble() < 0.1) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         Direction dir = (Direction)state.getValue(FACING);
         double side = random.nextDouble() * 0.6 - 0.3;
         double dx = dir.getAxis() == Axis.X ? (double)dir.getStepX() * 0.52 : side;
         double dz = dir.getAxis() == Axis.Z ? (double)dir.getStepZ() * 0.52 : side;
         level.addParticle(ParticleTypes.SMOKE, x + dx, y + random.nextDouble() * 6.0 / 16.0, z + dz, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.FLAME, x + dx, y + random.nextDouble() * 6.0 / 16.0, z + dz, 0.0, 0.0, 0.0);
      }
   }
}
