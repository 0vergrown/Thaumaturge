package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.thaumaturge.util.AspectMap;
import dev.overgrown.thaumaturge.util.CorruptionHelper;
import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.data_generator.ModItemTags;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.recipe.VesselRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VesselBlockEntity extends BlockEntity implements AspectContainer {
    public final static int MAX_SLUDGE_AMOUNT = 96;
    private final AspectMap aspects = new AspectMap();
    private final AspectMap sludgeAspects = new AspectMap();
    private boolean boiling = false;
    private int processTime = 0;
    private ItemStack processItems = ItemStack.EMPTY;

    public VesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VESSEL_BLOCK_ENTITY, pos, state);
    }


    public static void serverTick(World world, BlockPos pos, BlockState state, VesselBlockEntity blockEntity) {
        ServerWorld serverWorld = (ServerWorld) world;
        if (blockEntity.boiling) {
            if (world.getTime() % 10 == 0) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE_POP,
                        pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                        1, 0.1, 0.0, 0.1, 0.05);
            }

            blockEntity.processTime++;
            if (blockEntity.processTime >= 20) {
                blockEntity.processTime = 0;
                if (blockEntity.sludgeAspects.getTotalAspectLevel() < MAX_SLUDGE_AMOUNT) {
                    blockEntity.processItemForAspects(serverWorld);
                }
            }
        }

        // Check if water level is 0 and there are aspects
        int waterLevel = state.get(VesselBlock.WATER_LEVEL);
        if (waterLevel == 0 && !blockEntity.aspects.isEmpty()) {
            blockEntity.convertAspectsToVitium(serverWorld, pos);
        }
    }

    protected void convertAspectsToVitium(ServerWorld world, BlockPos pos) {
        // Calculate total aspects
        int totalAspects = this.aspects.getTotalAspectLevel() + sludgeAspects.getTotalAspectLevel();

        if (totalAspects == 0) {
            return;
        }

        CorruptionHelper.addCorruption(world, pos, totalAspects);

        // Clear aspects from vessel
        this.aspects.clear();
        this.sludgeAspects.clear();

        // Reset boiling state and process time
        this.boiling = false;
        this.processTime = 0;

        // Visual and sound effects
        world.spawnParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                10, 0.2, 0.2, 0.2, 0.0);
        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH,
                SoundCategory.BLOCKS, 1.0f, 1.0f);

        markDirty();
        syncToClient();
    }

    public void processItemForAspects(ServerWorld world) {

        if (processItems == ItemStack.EMPTY) return;

        AspectData aspectData = AspectsAPI.getAspectData(processItems);
        if (!aspectData.isEmpty()) {
            boolean noSludge = processItems.isIn(ModItemTags.VESSEL_NO_SLUDGE);
            for (var entry : aspectData.getMap().object2IntEntrySet()) {
                int totalAmount = entry.getIntValue();
                if (noSludge) {
                    aspects.modifyAspectLevel(entry.getKey(), totalAmount);
                } else {
                    int part_a = totalAmount / 2;
                    int part_b = totalAmount - part_a;

                    if (world.getRandom().nextBoolean()) {
                        aspects.modifyAspectLevel(entry.getKey(), part_a);
                        sludgeAspects.modifyAspectLevel(entry.getKey(), part_b);
                    } else {
                        aspects.modifyAspectLevel(entry.getKey(), part_b);
                        sludgeAspects.modifyAspectLevel(entry.getKey(), part_a);
                    }
                }
            }
            if (processItems.getCount() <= 1) {
                processItems = ItemStack.EMPTY;
            } else {
                processItems.decrement(1);
            }
        } else {
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, processItems.copy());
            world.spawnEntity(itemEntity);
            processItems = ItemStack.EMPTY;
        }
        markDirty();
        syncToClient();
    }

    public ItemStack addItem(ItemStack stack) {
        World world = getWorld();
        if (world == null) return stack;

        boolean isCatalyst = world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .anyMatch(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), stack));

        if (isCatalyst) {
            boolean recipeMatched = tryCraftWithCatalystDropped(stack);
            if (recipeMatched) {
                if (stack.getCount() < 1) {
                    return ItemStack.EMPTY;
                } else {
                    return stack.copyWithCount(stack.getCount()-1);
                }
            }
        }

        if (ItemStack.canCombine(stack, processItems)) {
            int sizeChange = Math.min(processItems.getMaxCount() - processItems.getCount(), stack.getCount());
            if (sizeChange > 0) {
                processItems.increment(sizeChange);
                int newSizeStack = stack.getCount() - sizeChange;
                return newSizeStack > 0 ? stack.copyWithCount(newSizeStack) : ItemStack.EMPTY;
            }
        } else if (processItems == ItemStack.EMPTY) {
            processItems = stack.copy();
            return ItemStack.EMPTY;
        }
        return stack;
    }



    public boolean tryCraftWithCatalystDropped(ItemStack catalystStack) {
        World world = getWorld();
        if (world == null) return false;
        if (sludgeAspects.getTotalAspectLevel() >= MAX_SLUDGE_AMOUNT) {return false;}

        boolean shouldConsume = tryCraftWithCatalyst(catalystStack);
        if (!shouldConsume && world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .anyMatch(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), catalystStack) &&
                        recipe.getAspects().entrySet().stream()
                                .allMatch(entry -> aspects.getAspectLevel(entry.getKey()) >= entry.getValue()))) {
            ItemEntity catalystEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, catalystStack.copy());
            world.spawnEntity(catalystEntity);
            return true;
        }
        return shouldConsume;
    }

    public boolean tryCraftWithCatalyst(ItemStack catalystStack) {
        World world = getWorld();
        if (world == null) return false;
        if (sludgeAspects.getTotalAspectLevel() >= MAX_SLUDGE_AMOUNT) {return false;}

        Optional<VesselRecipe> match = world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .filter(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), catalystStack))
                .filter(recipe -> recipe.getAspects().entrySet().stream()
                        .allMatch(entry -> aspects.getAspectLevel(entry.getKey()) >= entry.getValue()))
                .findFirst();

        if (match.isPresent()) {
            VesselRecipe recipe = match.get();

            recipe.getAspects().forEach((aspect, amount) -> {
                aspects.modifyAspectLevel(aspect, -amount);
            });

            ItemStack output = recipe.getOutput(world.getRegistryManager()).copy();
            ItemEntity result = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, output);
            world.spawnEntity(result);

            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof VesselBlock && world.random.nextFloat() < 0.3f) {
                int waterLevel = state.get(VesselBlock.WATER_LEVEL);
                if (waterLevel > 0) {
                    world.setBlockState(pos, state.with(VesselBlock.WATER_LEVEL, waterLevel - 1));
                }
            }

            markDirty();
            syncToClient();
            return recipe.consumesCatalyst();
        }
        return false;
    }


    @Override
    public Set<Identifier> getAspects() {
        return aspects.getAspects();
    }

    @Override
    public int getAspectLevel(@NotNull Identifier aspect) {
        return aspects.getAspectLevel(aspect);
    }

    @Override
    public @Nullable Integer getDesiredAspectLeve(@NotNull Identifier aspect) {
        return null;
    }

    @Override
    public boolean canReduceAspectLevels() {
        return true;
    }

    @Override
    public int getReducibleAspectLevel(@NotNull Identifier aspect) {
        return getAspectLevel(aspect);
    }

    @Override
    public void reduceAspectLevel(@NotNull Identifier aspect, int amount) {
        aspects.modifyAspectLevel(aspect, -amount);
        markDirty();
        syncToClient();
    }

    @Override
    public int increaseAspectLevel(@NotNull Identifier aspect, int amount) {
        aspects.modifyAspectLevel(aspect, amount);
        markDirty();
        syncToClient();
        return amount;

    }

    private void syncToClient() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    public void setBoiling(boolean boiling) {
        this.boiling = boiling;
        markDirty();
    }

    public boolean isBoiling() {
        return boiling;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put("ProcessItems", processItems.writeNbt(new NbtCompound()));

        NbtCompound aspectsNbt = aspects.toCompound();
        nbt.put("Aspects", aspectsNbt);
        NbtCompound sludgeAspectsNbt = sludgeAspects.toCompound();
        nbt.put("SludgeAspects", sludgeAspectsNbt);
        nbt.putBoolean("Boiling", boiling);
        nbt.putInt("ProcessTime", processTime);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        processItems = nbt.contains("ProcessItems") ? ItemStack.fromNbt(nbt.getCompound("ProcessItems")) : ItemStack.EMPTY;

        NbtCompound aspectsNbt = nbt.getCompound("Aspects");
        aspects.fromNbt(aspectsNbt);

        NbtCompound sludgeAspectsNbt = nbt.getCompound("SludgeAspects");
        sludgeAspects.fromNbt(sludgeAspectsNbt);

        boiling = nbt.getBoolean("Boiling");
        processTime = nbt.getInt("ProcessTime");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    public int getSludgeAmount() {
        return sludgeAspects.getTotalAspectLevel();
    }

    public Optional<ItemStack> removeItemStack() {
        if (processItems == ItemStack.EMPTY) return Optional.empty();
        ItemStack itemStack = processItems;
        processItems = ItemStack.EMPTY;
        markDirty();
        syncToClient();
        return Optional.ofNullable(itemStack);
    }

    public AspectMap removeSludge() {
        AspectMap map = new AspectMap();
        for (Identifier aspect : aspects.getAspects()) {
            int amount = sludgeAspects.getAspectLevel(aspect);
            int delta = amount / 3;
            map.setAspectLevel(aspect, amount);
            sludgeAspects.modifyAspectLevel(aspect, -delta);
        }
        markDirty();
        syncToClient();
        return map;
    }
}