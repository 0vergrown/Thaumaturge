package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModCreativeTabs {
    public static final ItemGroup ITEMS = ItemGroup.create(null,-1)
            .displayName(Text.translatable("itemGroup.thaumaturge"))
            .icon(ModItems.ORDO_CLUSTER::getDefaultStack)
            .entries((displayContext, entries) -> {
                entries.add(ModItems.MATRIX);
                entries.add(ModItems.PEDESTAL);
            })
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, Thaumaturge.identifier("items"),ITEMS);
    }

    private static RegistryKey<ItemGroup> register(String id) {
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(id));
    }
}
