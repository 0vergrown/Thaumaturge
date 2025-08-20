package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.aspectslib.api.AspectsAPI;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class GreaterFocusItem extends Item implements FocusItem {
    public GreaterFocusItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTier() {
        return "greater";
    }

    @Override
    public Identifier getAspect(ItemStack stack) {
        AspectData data = AspectsAPI.getAspectData(stack);
        if (!data.isEmpty()) {
            return data.getAspectIds().iterator().next(); // Get first aspect
        }
        return Thaumaturge.identifier("null");
    }

    @Override
    public Identifier getModifier(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("Modifier")) {
            return new Identifier(stack.getNbt().getString("Modifier"));
        }
        return Thaumaturge.identifier("stable");
    }

    @Override
    public void setModifier(ItemStack stack, Identifier modifier) {
        stack.getOrCreateNbt().putString("Modifier", modifier.toString());
    }
}