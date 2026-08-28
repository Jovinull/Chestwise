package dev.chestwise.minecraft;

//? if > 1.20.1 {
/*import com.mojang.serialization.MapCodec;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
/*? if >= 26.2 {*/
/*import net.minecraft.server.level.ServerLevel;
*//*?}*/
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class StorageTerminalBlock extends BaseEntityBlock {
    /** Cabinet plus work surface: the same for every facing. */
    private static final VoxelShape DESK = Block.box(0.0, 0.0, 0.0, 16.0, 11.0, 16.0);

    /** The catalogue shelf sits at the back, so it follows the block's facing. */
    private static final VoxelShape SHELF_NORTH = Block.box(1.0, 11.0, 9.0, 15.0, 15.0, 15.0);
    private static final VoxelShape SHELF_EAST = Block.box(1.0, 11.0, 1.0, 7.0, 15.0, 15.0);
    private static final VoxelShape SHELF_SOUTH = Block.box(1.0, 11.0, 1.0, 15.0, 15.0, 7.0);
    private static final VoxelShape SHELF_WEST = Block.box(9.0, 11.0, 1.0, 15.0, 15.0, 15.0);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(DESK, SHELF_NORTH);
    private static final VoxelShape SHAPE_EAST = Shapes.or(DESK, SHELF_EAST);
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(DESK, SHELF_SOUTH);
    private static final VoxelShape SHAPE_WEST = Shapes.or(DESK, SHELF_WEST);

    //? if > 1.20.1 {
    /*private static final MapCodec<StorageTerminalBlock> CODEC = simpleCodec(StorageTerminalBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    *///?}

    public StorageTerminalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The drawer front should look back at whoever placed the terminal.
        return defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    //? if < 26.2 {
    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean isMoving) {
        // The crafting grid lives on the block entity now, so it has to be
        // scattered when the terminal is destroyed or it would be lost.
        if (!state.is(newState.getBlock())) {
            releaseCraftingGrid(level, position);
        }
        super.onRemove(state, level, position, newState, isMoving);
    }
    //?} else {
    /*@Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos position, boolean movedByPiston) {
        releaseCraftingGrid(level, position);
        super.affectNeighborsAfterRemoval(state, level, position, movedByPiston);
    }
    *///?}

    /**
     * Puts the crafting grid back into the surrounding containers when the
     * terminal is destroyed, and scatters only what would not fit. deposit()
     * shrinks each stack in place, so nothing is duplicated or lost.
     */
    private static void releaseCraftingGrid(Level level, BlockPos position) {
        if (level.isClientSide()
            || !(level.getBlockEntity(position) instanceof StorageTerminalBlockEntity terminal)) {
            return;
        }
        // Menus can survive until the server processes the block removal. Clear
        // the shared output before moving inputs so no stale ResultSlot can be
        // taken during that short window.
        terminal.craftingResult().clearContent();
        NonNullList<ItemStack> grid = terminal.craftingGrid();
        for (ItemStack stack : grid) {
            if (!stack.isEmpty()) {
                terminal.deposit(stack, false);
            }
        }
        Containers.dropContents(level, position, grid);
        grid.clear();
        terminal.setChanged();
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
