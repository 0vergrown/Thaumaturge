package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.registry.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FocalManipulatorScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // Position of the inventory texture within the GUI (relative to GUI top‑left)
    public static final int INVENTORY_TEX_X = -65;
    public static final int INVENTORY_TEX_Y = -5;

    // Slot offsets inside the inventory texture (where the first slot is drawn)
    private static final int SLOT_OFFSET_X = -5;
    private static final int SLOT_OFFSET_Y = 5;
    private static final int HOTBAR_OFFSET_Y = 71; // Y offset from texture top to hotbar row

    // Slot positions (relative to GUI top‑left)
    private static final int INV_START_X = INVENTORY_TEX_X + SLOT_OFFSET_X;
    private static final int INV_START_Y = INVENTORY_TEX_Y + SLOT_OFFSET_Y;
    private static final int HOTBAR_Y = INVENTORY_TEX_Y + HOTBAR_OFFSET_Y;
    private static final int SLOT_SIZE = 18;

    // Client constructor
    public FocalManipulatorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1));
    }

    // Server constructor
    public FocalManipulatorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreens.FOCAL_MANIPULATOR, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // Focus slot (bottom left)
        this.addSlot(new Slot(inventory, 0, 31, 190) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof FocusItem;
            }
        });

        int xStart = -56;

        // Player main inventory (3 rows, 9 columns) - aligned with inventory texture
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 3 + 9,
                        xStart + col * SLOT_SIZE,
                        62 + row * SLOT_SIZE));
            }
        }
        // Player hotbar (1 row, 9 columns) - below main inventory
        for (int col = 0; col < 3; ++col) {
            for (int row = 0; row < 3; ++row) {
                this.addSlot(new Slot(playerInventory, col * 3 + row,
                        xStart + col * SLOT_SIZE,
                        5 + row * SLOT_SIZE));
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(slot);
        if (sourceSlot.hasStack()) {
            ItemStack stackInSlot = sourceSlot.getStack();
            originalStack = stackInSlot.copy();

            if (slot == 0) { // Focus slot -> player inventory
                if (!this.insertItem(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Player inventory -> focus slot or rearrange
                if (stackInSlot.getItem() instanceof FocusItem) {
                    if (!this.insertItem(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Move within player inventory (main <-> hotbar)
                    if (slot < 28) { // Main inventory -> hotbar
                        if (!this.insertItem(stackInSlot, 28, 37, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slot < 37) { // Hotbar -> main inventory
                        if (!this.insertItem(stackInSlot, 1, 28, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                sourceSlot.setStack(ItemStack.EMPTY);
            } else {
                sourceSlot.markDirty();
            }
            if (stackInSlot.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }
            sourceSlot.onTakeItem(player, stackInSlot);
        }
        return originalStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public Inventory getInventory() {
        return inventory;
    }
}