package com.styenvy.mcooxidizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OxidizerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public OxidizerBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state){
        return new OxidizerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                            @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != ModContent.OXIDIZER_BE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> OxidizerBlockEntity.serverTick(lvl, pos, st, (OxidizerBlockEntity) be);
    }

    @Override
    protected void onRemove(@NotNull BlockState state,
                            @NotNull Level level,
                            @NotNull BlockPos pos,
                            @NotNull BlockState newState,
                            boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!level.isClientSide && be instanceof OxidizerBlockEntity oxidizer) {
                for (int slot = 0; slot < OxidizerBlockEntity.SLOT_COUNT; slot++) {
                    ItemStack stack = oxidizer.getInv().getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                        oxidizer.getInv().setStackInSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(@NotNull BlockState state,
                                                  @NotNull Level level,
                                                  @NotNull BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof MenuProvider provider ? provider : null;
    }

    // 1.21.1: open GUI when empty-handed interaction
    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,
                                                     @NotNull Level level,
                                                     @NotNull BlockPos pos,
                                                     @NotNull Player player,
                                                     @NotNull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) {
                sp.openMenu(provider);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // Also open when the player is holding an item (so it works regardless of hand contents)
    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack,
                                                    @NotNull BlockState state,
                                                    @NotNull Level level,
                                                    @NotNull BlockPos pos,
                                                    @NotNull Player player,
                                                    @NotNull InteractionHand hand,
                                                    @NotNull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) {
                sp.openMenu(provider);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
