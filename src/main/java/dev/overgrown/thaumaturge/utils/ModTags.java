/**
 * ModTags.java
 * <p>
 * Defines tags for blocks and items that can be used in recipes and for categorization.
 * Tags are used throughout the mod for various purposes like identifying foci items.
 */
package dev.overgrown.thaumaturge.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Blocks {
        /**
         * Helper method to create a block tag with the mod's namespace
         */
        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Thaumaturge.MOD_ID, name));
        }

        // Example block tag - can be used to group blocks with similar properties
        public static final TagKey<Block> EXAMPLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("example_block"));
    }

    public static class Items {
        /**
         * Helper method to create an item tag with the mod's namespace
         */
        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(Thaumaturge.MOD_ID, name));
        }

        // Tag for all foci items - used to identify items that can be inserted into gauntlets
        public static final TagKey<Item> FOCI = TagKey.of(RegistryKeys.ITEM, Thaumaturge.identifier("foci"));
    }
}