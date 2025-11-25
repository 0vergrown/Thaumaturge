package dev.overgrown.thaumaturge.item.gauntlet;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicCastingGauntletItem extends ResonanceGauntletItem implements DyeableItem {

    public BasicCastingGauntletItem(Settings settings) {
        super(settings, 1);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if (world != null) {
            ArmorTrim.getTrim(world.getRegistryManager(), stack).ifPresent(trim ->
                    ArmorTrim.appendTooltip(stack, world.getRegistryManager(), tooltip)
            );
        }
    }

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt("display");
        return nbtCompound != null && nbtCompound.contains("color", NbtElement.NUMBER_TYPE)
                ? nbtCompound.getInt("color")
                : getDefaultColor(); // Use a method for default color
    }

    private int getDefaultColor() {
        return 0xA06540; // Default leather color
    }
}