package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AdvancedFocusItem extends Item implements FocusItem {
    public AdvancedFocusItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTier() {
        return "advanced";
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