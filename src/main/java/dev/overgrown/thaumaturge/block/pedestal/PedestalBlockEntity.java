package dev.overgrown.thaumaturge.block.pedestal;

import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class PedestalBlockEntity extends BlockEntity {
    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PEDESTAL_BE, pos, state);
    }

    private final SimpleInventory inventory = new SimpleInventory(1) {
        @Override
        public void setStack(int slot, ItemStack stack) {
            super.setStack(slot, stack);
            PedestalBlockEntity.this.markDirty();
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    };

    public ItemStack getItem() {
        return inventory.getStack(0);
    }

    public void setItem(ItemStack stack){
        inventory.setStack(0, stack);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory.stacks);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        writeNbt(nbt, this.inventory.stacks);
    }

    public static void writeNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        NbtList nbtList = new NbtList();

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack itemStack = stacks.get(i);
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);
            itemStack.writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
        }
        nbt.put("Items", nbtList);
    }
}
