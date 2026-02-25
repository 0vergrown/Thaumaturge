package dev.overgrown.thaumaturge.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends Block implements BlockEntityProvider {
    public PedestalBlock(Settings settings) {
        super(settings);
    }

    private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
    private static final VoxelShape X_STEP_SHAPE = Block.createCuboidShape(3.0, 4.0, 4.0, 13.0, 5.0, 12.0);
    private static final VoxelShape X_STEM_SHAPE = Block.createCuboidShape(4.0, 5.0, 6.0, 12.0, 10.0, 10.0);
    private static final VoxelShape X_FACE_SHAPE = Block.createCuboidShape(0.0, 10.0, 3.0, 16.0, 16.0, 13.0);
    private static final VoxelShape X_AXIS_SHAPE = VoxelShapes.union(BASE_SHAPE, X_STEP_SHAPE, X_STEM_SHAPE, X_FACE_SHAPE);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return X_AXIS_SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PedestalBlockEntity pedestalBlockEntity) {
            ItemStack pedestalStack = pedestalBlockEntity.getItem();
            if (pedestalStack.isEmpty()) {
                if (!handStack.isEmpty()) {
                    if (!world.isClient) {
                        ItemStack split = handStack.split(1);
                        pedestalBlockEntity.setItem(split);
                        //player.setStackInHand(hand, pedestalStack);
                    }
                    return ActionResult.success(world.isClient);
                }
            } else {
                if (handStack.isEmpty()) {
                    if (!world.isClient) {
                        player.setStackInHand(hand,pedestalStack);
                        pedestalBlockEntity.setItem(ItemStack.EMPTY);
                    }
                    return ActionResult.success(world.isClient);
                }
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }
}
