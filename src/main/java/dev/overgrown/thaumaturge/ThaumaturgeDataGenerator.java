package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.data_generator.ModBlockLootTables;
import dev.overgrown.thaumaturge.data_generator.ModBlockTags;
import dev.overgrown.thaumaturge.data_generator.ModItemTags;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ThaumaturgeDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModBlockLootTables::new);
        pack.addProvider(ModBlockTags::new);
        pack.addProvider(ModItemTags::new);
    }
}