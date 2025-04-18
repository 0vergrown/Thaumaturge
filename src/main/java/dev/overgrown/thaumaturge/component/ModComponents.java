/**
 * ModComponents.java
 * <p>
 * This class registers and manages all data components used by the Thaumaturge mod.
 * Components are a system to attach additional data to vanilla objects (items, entities, etc.).
 * <p>
 * The mod uses components to:
 * - Track gauntlet foci capacity (MAX_FOCI)
 * - Store which foci are equipped in a gauntlet (GAUNTLET_STATE)
 * - Track Apophenia book progress (BOOK_STATE)
 * - Store aspect information on items/blocks (ASPECT)
 * <p>
 * Each component is registered to the game's component registry with appropriate
 * codecs for serialization and packet handling.
 *
 * @see dev.overgrown.thaumaturge.component.GauntletComponent
 * @see dev.overgrown.thaumaturge.component.BookStateComponent
 * @see dev.overgrown.thaumaturge.component.AspectComponent
 */
package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class ModComponents {
    /**
     * Registers all component types for the Thaumaturge mod
     */
    public static void register() {
        Thaumaturge.LOGGER.info("Registering Mod Components for " + Thaumaturge.MOD_ID);
    }

    /**
     * Helper method to register a component type with customized builder options
     *
     * @param path Path for the component's identifier
     * @param operator Function to configure the component builder
     * @return The registered component type
     */
    private static <T> ComponentType<T> register(String path, UnaryOperator<ComponentType.Builder<T>> operator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Thaumaturge.identifier(path), operator.apply(new ComponentType.Builder<>()).build());
    }

    /**
     * Defines the maximum number of foci a gauntlet can hold
     * Used on gauntlet items to determine their capacity
     */
    public static final ComponentType<Integer> MAX_FOCI = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Thaumaturge.identifier("max_foci"),
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    /**
     * Stores which foci are currently equipped in a gauntlet
     * References foci by their registry identifiers
     *
     * @see dev.overgrown.thaumaturge.component.GauntletComponent
     */
    public static final ComponentType<GauntletComponent> GAUNTLET_STATE = register("gauntlet_state", builder -> builder
            .codec(GauntletComponent.CODEC)
            .packetCodec(GauntletComponent.PACKET_CODEC));

    public static final ComponentType<BookStateComponent> BOOK_STATE = register("book_state", builder -> builder
        .codec(BookStateComponent.CODEC)
        .packetCodec(BookStateComponent.PACKET_CODEC));

    public static final ComponentType<AspectComponent> ASPECT = register("aspects", builder -> builder
        .codec(AspectComponent.CODEC)
        .packetCodec(AspectComponent.PACKET_CODEC));

}
