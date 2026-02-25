package dev.overgrown.thaumaturge.block.alchemical_furnace;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.item.alchemical_sludge_bottle.AlchemicalSludgeBottleItem;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.screen.AlchemicalFurnaceScreenHandler;
import dev.overgrown.thaumaturge.util.AspectMap;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class AlchemicalFurnaceBlockEntity extends BlockEntity implements AspectContainer, SidedInventory, NamedScreenHandlerFactory {

    public static final int MAX_ASPECT_COUNT = 200;
    public static final  int DEFAULT_BURN_TIME = 150;
    public static final int ALCHEMICAL_SLUDGE_BURN_TIME = 600;
    public static final int INPUT_SLOT = 0;
    public static final int[] TOP_SLOT = {INPUT_SLOT};
    public static final int FUEL_SLOT = 1;
    public static final int[] SIDE_SLOT = {FUEL_SLOT};
    public static final int OUTPUT_SLOT = 2;
    public static final int[] BOTTOM_SLOT = {OUTPUT_SLOT};

    private final AspectMap aspects = new AspectMap();
    int fuelBurnTime = 0;
    private int itemBurnTime = 0;
    private int fuelMaxBurnTime = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    // Cache Burn Process as long as the container is not modified
    private boolean updateCache = true;
    private boolean canConvertItem = false;
    private int itemMaxBurnTime = 0;



    public AlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ALCHEMICAL_FURNACE_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, AlchemicalFurnaceBlockEntity blockEntity) {
        blockEntity.optionalCacheUpdate();
        blockEntity.handleFuel((ServerWorld) world);
        if (blockEntity.canConvertItem) {
            blockEntity.handleProcess();
        }
    }

    private void optionalCacheUpdate() {
        if (updateCache) {
            ItemStack itemStack = inventory.get(INPUT_SLOT);
            Item inputItem = itemStack.getItem();

            if (itemStack.isEmpty()) {
                canConvertItem = false;
            } else if ((inputItem == ModItems.ALCHEMICAL_SLUDGE_BOTTLE) && canAddBottleToOutputSlot()) {
                canConvertItem = true;
                itemMaxBurnTime = ALCHEMICAL_SLUDGE_BURN_TIME;
            } else if (!AspectsAPI.getItemAspectData(inputItem).isEmpty()) {
                canConvertItem = true;
                itemMaxBurnTime = DEFAULT_BURN_TIME;
            }
            canConvertItem = canConvertItem && (aspects.getTotalAspectLevel() <= MAX_ASPECT_COUNT);
            updateCache = false;

            if (!canConvertItem) {
                itemBurnTime = 0;
                itemMaxBurnTime = 0;
                markDirty();
                syncToClient();
            }
        }

    }
    private void handleProcess() {
        if (fuelBurnTime > 0) {
            if (itemBurnTime >= itemMaxBurnTime) {
                itemBurnTime = 0;
                ItemStack input = inventory.get(INPUT_SLOT);
                if (input.getItem().equals(ModItems.ALCHEMICAL_SLUDGE_BOTTLE)) {
                    AlchemicalSludgeBottleItem.addToAspectMap(input,aspects);
                    inventory.set(INPUT_SLOT, ItemStack.EMPTY);
                    ItemStack output = inventory.get(OUTPUT_SLOT);
                    // Blindly add Glass Bottle as the ability to do so is guaranteed by canConvertItem being true
                    if (output.getItem().equals(Items.GLASS_BOTTLE)) {
                        output.increment(1);
                    } else {
                        inventory.set(OUTPUT_SLOT, new ItemStack(Items.GLASS_BOTTLE));
                    }
                } else {
                    AspectData aspectData = AspectsAPI.getAspectData(input);
                    for (Identifier aspect : aspectData.getAspectIds()) {
                        aspects.modifyAspectLevel(aspect,aspectData.getLevel(aspect));
                    }
                    input.decrement(1);
                    if (input.isEmpty()) {
                        inventory.set(INPUT_SLOT, ItemStack.EMPTY);
                    }
                }
                updateCache = true;
            } else {
                itemBurnTime++;
            }
        }
        markDirty();
        syncToClient();
    }

    private void handleFuel(ServerWorld world) {
        if (fuelBurnTime > 0) {
            fuelBurnTime--;
            markDirty();
            syncToClient();
        } else if (canConvertItem) {
            ItemStack fuel = inventory.get(FUEL_SLOT);
            Integer burnTime = FuelRegistry.INSTANCE.get(fuel.getItem());
            if (burnTime != null) {
                if (fuel.getCount() <= 1) {
                    ItemStack remainder = fuel.getRecipeRemainder();
                    inventory.set(FUEL_SLOT,remainder);
                } else {
                    fuel.decrement(1);
                }
                world.setBlockState(pos, getCachedState().with(AlchemicalFurnaceBlock.LIT, true));
                fuelBurnTime = burnTime;
                fuelMaxBurnTime = burnTime;
                markDirty();
                syncToClient();
            } else if (getCachedState().get(AlchemicalFurnaceBlock.LIT)) {
                this.itemBurnTime = 0;
                this.itemMaxBurnTime = 0;
                markDirty();
                world.setBlockState(pos, getCachedState().with(AlchemicalFurnaceBlock.LIT, false));
            }
        } else {
            if (getCachedState().get(AlchemicalFurnaceBlock.LIT)) {
                world.setBlockState(pos, getCachedState().with(AlchemicalFurnaceBlock.LIT, false));
            }
        }
    }

    private boolean canAddBottleToOutputSlot() {
        ItemStack outputStack = inventory.get(OUTPUT_SLOT);
        return  (outputStack == ItemStack.EMPTY) ||
                (ItemStack.canCombine(outputStack, new ItemStack(Items.GLASS_BOTTLE)));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtCompound aspectsNbt = aspects.toCompound();
        nbt.put("Aspects", aspectsNbt);
        nbt.putInt("FuelBurnTime", fuelBurnTime);
        nbt.putInt("ItemBurnTime", itemBurnTime);
        nbt.putInt("FuelMaxBurnTime", fuelMaxBurnTime);
        Inventories.writeNbt(nbt, inventory);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        NbtCompound aspectsNbt = nbt.getCompound("Aspects");
        aspects.fromNbt(aspectsNbt);
        fuelBurnTime = nbt.getInt("FuelBurnTime");
        itemBurnTime = nbt.getInt("ItemBurnTime");
        fuelMaxBurnTime = nbt.getInt("FuelMaxBurnTime");
        Inventories.readNbt(nbt, inventory);

        // Update Caches
        this.updateCache = true;
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

    @Override
    public @Unmodifiable Set<Identifier> getAspects() {
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
        return aspects.getAspectLevel(aspect);
    }

    @Override
    public void reduceAspectLevel(@NotNull Identifier aspect, int amount) {
        int prev = aspects.getTotalAspectLevel();
        aspects.modifyAspectLevel(aspect,-amount);
        int post = aspects.getTotalAspectLevel();
        if ((prev > MAX_ASPECT_COUNT ) && ( post <= MAX_ASPECT_COUNT)) {
            updateCache = true;
        }
        markDirty();
        syncToClient();
    }

    @Override
    public int increaseAspectLevel(@NotNull Identifier aspect, int amount) {
        // The Alchemical Furnace does not accept any transfer of aspects into itself
        return 0;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side.getAxis().isHorizontal()) {
            return SIDE_SLOT;
        } else if (side.equals(Direction.UP)) {
            return TOP_SLOT;
        } else {
            return BOTTOM_SLOT;
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if(dir == null) {
            return true;
        } else if (dir.getAxis().isHorizontal() && slot == FUEL_SLOT) {
            return true;
        } else if (dir.equals(Direction.UP) && slot == INPUT_SLOT) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT && dir.equals(Direction.UP);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        updateCache = true;
        ItemStack stack = this.inventory.get(slot);
        if (amount >= stack.getCount()) {
            this.inventory.set(slot, ItemStack.EMPTY);
            markDirty();
            return stack;
        } else {
            markDirty();
            return stack.split(amount);
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        updateCache = true;
        ItemStack stack = this.inventory.get(slot);
        this.inventory.set(slot, ItemStack.EMPTY);
        markDirty();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        updateCache = true;
        ItemStack stackOld = this.inventory.get(slot);
        if (slot == INPUT_SLOT && !stackOld.getItem().equals(stack.getItem())) {
            this.itemBurnTime = 0;
            this.itemMaxBurnTime = 0;
        }
        this.inventory.set(slot, stack);
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        updateCache = true;
        this.inventory.clear();
    }

    private void syncToClient() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    public int getTotalAspectAmount() {
        return aspects.getTotalAspectLevel();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new AlchemicalFurnaceScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fuelBurnTime;
                case 1 -> itemBurnTime;
                case 2 -> itemMaxBurnTime;
                case 3 -> aspects.getTotalAspectLevel();
                case 4 -> fuelMaxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {

        }

        //this is supposed to return the amount of integers you have in your delegate, in our example only one
        @Override
        public int size() {
            return 5;
        }
    };
}
