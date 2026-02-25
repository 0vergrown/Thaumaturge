package dev.overgrown.thaumaturge.screen;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.thaumaturge.block.alchemical_furnace.AlchemicalFurnaceBlockEntity;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.registry.ModScreens;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AlchemicalFurnaceScreenHandler extends ScreenHandler {

    private static class FuelSlot extends Slot {
        public FuelSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
        @Override
        public boolean canInsert(ItemStack stack) {
            Integer fuel = FuelRegistry.INSTANCE.get(stack.getItem());
            return fuel != null && super.canInsert(stack);
        }
    }

    private static class OutputSlot extends Slot {

        public OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }

    private static class InputSLot extends Slot {

        public InputSLot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            boolean hasAspects = !AspectsAPI.getItemAspectData(stack.getItem()).isEmpty() || stack.getItem().equals(ModItems.ALCHEMICAL_SLUDGE_BOTTLE);
            return hasAspects && super.canInsert(stack);
        }
    }

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final Slot inputSlot;
    private final Slot fuelSlot;


    // Server Constructor
    public AlchemicalFurnaceScreenHandler (int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(ModScreens.ALCHEMICAL_FURNACE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        this.propertyDelegate = delegate;

        inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);

        // XY Cords are those for the container gui image

        // Add Furnace Slots (Index 0-2)
        this.inputSlot = new InputSLot(this.inventory, AlchemicalFurnaceBlockEntity.INPUT_SLOT, 62, 17);
        this.addSlot(inputSlot);
        this.fuelSlot = new FuelSlot(this.inventory, AlchemicalFurnaceBlockEntity.FUEL_SLOT, 62, 53);
        this.addSlot(fuelSlot);
        this.addSlot(new OutputSlot(this.inventory, AlchemicalFurnaceBlockEntity.OUTPUT_SLOT, 98, 53));

        // Inventory (Index 3-26)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar (Index 27-36)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    // Client Constructor
    public AlchemicalFurnaceScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(5));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(slot);
        if (sourceSlot.hasStack()) {
            ItemStack oldStack = sourceSlot.getStack();
            ItemStack oldStackCopy = oldStack.copy();

            // Case: Shifting items back to player inventory
            if (sourceSlot.inventory == this.inventory) {
                // Try to insert the stack into the player inventory
                if(!this.insertItem(oldStack, 3, 39, true)) {
                    return ItemStack.EMPTY; // All Items Inserted, direct return
                }
                sourceSlot.onQuickTransfer(oldStack, oldStackCopy);
            }
            // Case: Shifting Items into the furnace inventory
            else {
                if (this.inputSlot.canInsert(oldStack)) {
                    this.inputSlot.insertStack(oldStack);
                }
                if(this.fuelSlot.canInsert(oldStack)) {
                    this.fuelSlot.insertStack(oldStack);
                }

            }

            if (oldStack.isEmpty()) {
                sourceSlot.setStack(ItemStack.EMPTY);
            } else {
                sourceSlot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public int getFuelBurnTime() {
        return propertyDelegate.get(0);
    }

    public int getItemBurnTime() {
        return propertyDelegate.get(1);
    }

    public int getItemMaxBurnTime() {
        return propertyDelegate.get(2);
    }

    public int getTotalAspectLevel() {
        return propertyDelegate.get(3);
    }

    public int getFuelMaxBurnTime() {
        return propertyDelegate.get(4);
    }
}
