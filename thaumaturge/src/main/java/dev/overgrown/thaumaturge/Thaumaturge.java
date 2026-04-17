package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.block.vessel.AspectReactionPrimitiveManager;
import dev.overgrown.thaumaturge.networking.FocalManipulatorPackets;
import dev.overgrown.thaumaturge.networking.GauntletCastPackets;
import dev.overgrown.thaumaturge.recipe.VesselRecipe;
import dev.overgrown.thaumaturge.registry.*;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffectRegistry;
import dev.overgrown.thaumaturge.spell.focal.FocalComponentRegistry;
import dev.overgrown.thaumaturge.spell.impl.effects.*;
import dev.overgrown.thaumaturge.spell.input.GauntletInputTracker;
import dev.overgrown.thaumaturge.spell.input.GauntletInteractionBlocklist;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Thaumaturge implements ModInitializer {
    public static final String MOD_ID = "thaumaturge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Items
        ModItems.initialize();

        // Blocks
        ModBlocks.initialize();

        // Sounds
        ModSounds.initialize();

        // Screens
        ModScreens.initialise();

        // Creative Tabs
        ModCreativeTabs.initialize();

        // World Generation
        ModWorldGen.initialize();

        // Register vessel recipe type and serializer
        Registry.register(Registries.RECIPE_SERIALIZER, identifier("vessel"), VesselRecipe.Serializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, identifier("vessel"), VesselRecipe.Type.INSTANCE);

        // Focal Manipulator spell components
        FocalComponentRegistry.init();
        FocalManipulatorPackets.registerServerReceivers();

        // Spell Effects
        GauntletSpellEffectRegistry.register(new IgnisEffect());
        GauntletSpellEffectRegistry.register(new AerEffect());
        GauntletSpellEffectRegistry.register(new GelumEffect());
        GauntletSpellEffectRegistry.register(new TerraEffect());
        GauntletSpellEffectRegistry.register(new VitiumEffect());
        GauntletSpellEffectRegistry.register(new MortuusEffect());
        GauntletSpellEffectRegistry.register(new VictusEffect());
        GauntletSpellEffectRegistry.register(new PerditioEffect());
        GauntletSpellEffectRegistry.register(new PermutatioEffect());
        GauntletSpellEffectRegistry.register(new AlienisEffect());
        GauntletSpellEffectRegistry.register(new MotusEffect());
        GauntletSpellEffectRegistry.register(new AquaEffect());

        // Combo casting system:

        // Client-side combo detection sends C2S packet → server receiver fires spell
        GauntletCastPackets.registerServerReceiver();

        // Server-tick cleanup for the combo tracker (legacy, kept for cleanup)
        GauntletInputTracker.initialize();

        // Block interaction suppression
        // These blocks have onUse() that consumes right-click; we must not also
        // count that click as a gauntlet RIGHT input.
        GauntletInteractionBlocklist.register(ModBlocks.VESSEL);
        GauntletInteractionBlocklist.register(ModBlocks.FAUCET);
        GauntletInteractionBlocklist.register(ModBlocks.JAR);
        GauntletInteractionBlocklist.register(ModBlocks.ALCHEMICAL_FURNACE);
        GauntletInteractionBlocklist.register(ModBlocks.FOCAL_MANIPULATOR);

        // Resource Manager
        ResourceManagerHelper server = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        server.registerReloadListener(new AspectReactionPrimitiveManager());

        LOGGER.info("Thaumaturge initialized!");
    }
}