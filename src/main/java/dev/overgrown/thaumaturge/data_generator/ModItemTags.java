package dev.overgrown.thaumaturge.data_generator;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

import java.util.concurrent.CompletableFuture;

public class ModItemTags extends FabricTagProvider.ItemTagProvider {

    public static final TagKey<Item> VESSEL_NO_SLUDGE = TagKey.of(RegistryKeys.ITEM, Thaumaturge.identifier("vessel_no_sludge"));

    // Create a custom dyeable tag
    public static final TagKey<Item> DYEABLE = TagKey.of(RegistryKeys.ITEM, Thaumaturge.identifier("dyeable"));

    public ModItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(VESSEL_NO_SLUDGE)
                .add(AspectsLib.identifier("aer_aspect_shard"))
                .add(AspectsLib.identifier("aqua_aspect_shard"))
                .add(AspectsLib.identifier("ignis_aspect_shard"))
                .add(AspectsLib.identifier("ordo_aspect_shard"))
                .add(AspectsLib.identifier("perditio_aspect_shard"))
                .add(AspectsLib.identifier("terra_aspect_shard"))
                .add(AspectsLib.identifier("gelum_aspect_shard"))
                .add(AspectsLib.identifier("lux_aspect_shard"))
                .add(AspectsLib.identifier("metallum_aspect_shard"))
                .add(AspectsLib.identifier("mortuus_aspect_shard"))
                .add(AspectsLib.identifier("motus_aspect_shard"))
                .add(AspectsLib.identifier("permutatio_aspect_shard"))
                .add(AspectsLib.identifier("potentia_aspect_shard"))
                .add(AspectsLib.identifier("vacuos_aspect_shard"))
                .add(AspectsLib.identifier("victus_aspect_shard"))
                .add(AspectsLib.identifier("vitreus_aspect_shard"))
                .add(AspectsLib.identifier("bestia_aspect_shard"))
                .add(AspectsLib.identifier("exanimis_aspect_shard"))
                .add(AspectsLib.identifier("herba_aspect_shard"))
                .add(AspectsLib.identifier("instrumentum_aspect_shard"))
                .add(AspectsLib.identifier("praecantatio_aspect_shard"))
                .add(AspectsLib.identifier("spiritus_aspect_shard"))
                .add(AspectsLib.identifier("tenebrae_aspect_shard"))
                .add(AspectsLib.identifier("vinculum_aspect_shard"))
                .add(AspectsLib.identifier("volatus_aspect_shard"))
                .add(AspectsLib.identifier("alienis_aspect_shard"))
                .add(AspectsLib.identifier("alkimia_aspect_shard"))
                .add(AspectsLib.identifier("auram_aspect_shard"))
                .add(AspectsLib.identifier("aversio_aspect_shard"))
                .add(AspectsLib.identifier("cognitio_aspect_shard"))
                .add(AspectsLib.identifier("desiderium_aspect_shard"))
                .add(AspectsLib.identifier("fabrico_aspect_shard"))
                .add(AspectsLib.identifier("humanus_aspect_shard"))
                .add(AspectsLib.identifier("machina_aspect_shard"))
                .add(AspectsLib.identifier("praemunio_aspect_shard"))
                .add(AspectsLib.identifier("sensus_aspect_shard"))
                .add(AspectsLib.identifier("vitium_aspect_shard"))
                .add(AspectsLib.identifier("fames_aspect_shard"));

        // Add gauntlet to custom dyeable items
        getOrCreateTagBuilder(DYEABLE)
                .add(ModItems.BASIC_CASTING_GAUNTLET);

        // Add gauntlet to trimmable armor
        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.BASIC_CASTING_GAUNTLET);
    }
}