package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.aspectslib.api.AspectsAPI;
import dev.overgrown.aspectslib.data.AspectData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LesserFocusItem extends Item implements FocusItem {
    public LesserFocusItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTier() {
        return "lesser";
    }

    @Override
    public Identifier getAspect(ItemStack stack) {
        AspectData data = AspectsAPI.getAspectData(stack);
        if (!data.isEmpty()) {
            return data.getAspectIds().iterator().next(); // Get first aspect
        }
        return new Identifier("thaumaturge", "null");
    }

    @Override
    public Identifier getModifier(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("Modifier")) {
            return new Identifier(stack.getNbt().getString("Modifier"));
        }
        return new Identifier("thaumaturge", "stable");
    }

    @Override
    public void setModifier(ItemStack stack, Identifier modifier) {
        stack.getOrCreateNbt().putString("Modifier", modifier.toString());
    }
}