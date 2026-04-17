package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.alchemical_sludge_bottle.AlchemicalSludgeBottleItem;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class VesselBlock extends BlockWithEntity {
    public static final IntProperty WATER_LEVEL = IntProperty.of("water_level", 0, 3);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public VesselBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(WATER_LEVEL, 0));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATER_LEVEL);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VesselBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlocks.VESSEL_BLOCK_ENTITY, VesselBlockEntity::serverTick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (stack.isOf(Items.DEBUG_STICK) && !world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            AspectReactionHolder.Provider reactionProvider = (AspectReactionHolder.Provider) serverWorld.getServer();
            AspectReactionHolder reactionHolder = reactionProvider.thaumaturge$getAspectReactionHolder();
            Thaumaturge.LOGGER.info(reactionHolder.toString());
        }

        if (!(blockEntity instanceof VesselBlockEntity vessel)) {
            return ActionResult.PASS;
        }

        if (stack.getItem() == Items.WATER_BUCKET && state.get(WATER_LEVEL) < 3) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, 3));
                if (!player.isCreative()) {
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                }
                updateBoilingState(world, pos, state.with(WATER_LEVEL, 3));
            }
            return ActionResult.SUCCESS;
        }

        if (stack.getItem() == Items.BUCKET && state.get(WATER_LEVEL) == 3) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, 0));
                if (!player.isCreative()) {
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
                    } else {
                        player.getInventory().insertStack(new ItemStack(Items.WATER_BUCKET));
                    }
                }
                updateBoilingState(world, pos, state.with(WATER_LEVEL, 0));
            }
            return ActionResult.SUCCESS;
        }

        if (stack.getItem() == Items.GLASS_BOTTLE && vessel.getSludgeAmount() > 0 && state.get(WATER_LEVEL) > 0) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, state.get(WATER_LEVEL) - 1));
                stack.decrement(1);

                ItemStack sludgeBottle = AlchemicalSludgeBottleItem.fromAspectMap(vessel.removeSludge());
                if (stack.isEmpty())  {
                    player.setStackInHand(hand, sludgeBottle);
                } else {
                    player.getInventory().insertStack(sludgeBottle);
                }
            }
            return ActionResult.SUCCESS;
        }

        if (!stack.isEmpty() && state.get(WATER_LEVEL) > 0) {
            if (!world.isClient) {
                boolean shouldConsume = vessel.tryCraftWithCatalyst(stack);
                if (shouldConsume) {
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    return ActionResult.SUCCESS;
                }
                
                if (vessel.isBoiling()) {
                    ItemStack remainder = vessel.addItem(stack);

                    player.setStackInHand(hand, remainder);
                    if (stack.isEmpty() || remainder != stack) {
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.CONSUME;
        }
        if (stack.isEmpty()) {
            if (!world.isClient) {
                Optional<ItemStack> itemStack = vessel.removeItemStack();
                itemStack.ifPresent(value -> player.setStackInHand(hand, value));
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static int getTemperatureFromBelow(World world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);
        if (belowState.isOf(Blocks.SOUL_FIRE) || belowState.isOf(Blocks.SOUL_CAMPFIRE)) {
            return 4;
        }  else if (belowState.isOf(Blocks.LAVA) || belowState.isOf(Blocks.LAVA_CAULDRON)) {
            return 3;
        }  else if (belowState.isOf(Blocks.FIRE) || belowState.isOf(Blocks.CAMPFIRE)) {
            return 2;
        } else if (belowState.isOf(Blocks.TORCH)) {
            return 1;
        } else if (belowState.isOf(Blocks.ICE) || belowState.isOf(Blocks.SNOW_BLOCK)) {
            return -1;
        } else if (belowState.isOf(Blocks.BLUE_ICE)) {
            return -2;
        } else {
            return 0;
        }
    }

    private void updateBoilingState(World world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof VesselBlockEntity vessel) {
            vessel.setTemperature(getTemperatureFromBelow(world, pos));
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (world.getBlockEntity(pos) instanceof VesselBlockEntity vessel) {
            vessel.setTemperature(getTemperatureFromBelow(world, pos));
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    // If the block is destroyed add aspects as corruption to the world
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world instanceof ServerWorld serverWorld) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof VesselBlockEntity vesselBlockEntity) {
                    vesselBlockEntity.convertAspectsToVitium(serverWorld, pos);
                }
            }
            super.onStateReplaced(state,world,pos,newState,moved);
        }
    }
}