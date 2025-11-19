package net.stirdrem.overgeared.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSmithingAnvilNew extends BlockWithEntity implements BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape Z1 = Block.createCuboidShape(3, 9, 0, 13, 16, 16);
    private static final VoxelShape Z2 = Block.createCuboidShape(3, 0, 1, 13, 3, 15);
    private static final VoxelShape Z3 = Block.createCuboidShape(4, 0, 4, 12, 3, 12);
    private static final VoxelShape Z4 = Block.createCuboidShape(5, 3, 3, 11, 4, 13);
    private static final VoxelShape Z5 = Block.createCuboidShape(6, 4, 4, 10, 9, 12);

    private static final VoxelShape X1 = Block.createCuboidShape(0, 9, 3, 16, 16, 13);
    private static final VoxelShape X2 = Block.createCuboidShape(1, 0, 3, 15, 3, 13);
    private static final VoxelShape X3 = Block.createCuboidShape(4, 0, 4, 12, 3, 12);
    private static final VoxelShape X4 = Block.createCuboidShape(3, 3, 5, 13, 4, 11);
    private static final VoxelShape X5 = Block.createCuboidShape(4, 4, 6, 12, 9, 10);

    // Combined shapes using Fabric/Yarn VoxelShapes.union()
    private static final VoxelShape X_AXIS_AABB = VoxelShapes.union(X1, X2, X3, X4, X5);
    private static final VoxelShape Z_AXIS_AABB = VoxelShapes.union(Z1, Z2, Z3, Z4, Z5);

    public AbstractSmithingAnvilNew(Settings settings) {
        super(settings);
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().rotateYClockwise());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null; // Override in subclasses
    }
}
