package dev.chestwise.minecraft;

//? if > 1.20.1 {
/*import com.mojang.serialization.MapCodec;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class StorageTerminalBlock extends BaseEntityBlock {
    //? if > 1.20.1 {
    /*private static final MapCodec<StorageTerminalBlock> CODEC = simpleCodec(StorageTerminalBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    *///?}

    public StorageTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new StorageTerminalBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //? if < 26.2 {
        return level.isClientSide ? null : createTickerHelper(
        //?} else {
        /*return level.isClientSide() ? null : createTickerHelper(
        *///?}
            type,
            ChestwiseContent.STORAGE_TERMINAL_ENTITY,
            StorageTerminalBlockEntity::serverTick
        );
    }

    //? if <= 1.20.1 {
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(
        BlockState state,
        Level level,
        BlockPos position,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        if (!level.isClientSide) {
            MenuProvider provider = state.getMenuProvider(level, position);
            if (provider != null && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    //?}

    //? if > 1.20.1 {
    /*@Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos position,
        Player player,
        BlockHitResult hit
    ) {
        //? if < 26.2 {
        if (!level.isClientSide) {
        //?} else {
        if (!level.isClientSide()) {
        //?}
            MenuProvider provider = state.getMenuProvider(level, position);
            if (provider != null && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider);
            }
        }
        //? if < 26.2 {
        return InteractionResult.sidedSuccess(level.isClientSide);
        //?} else {
        return InteractionResult.SUCCESS;
        //?}
    }
    *///?}
}
