package dev.overgrown.thaumaturge.util;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.corruption.CorruptionDataManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public class CorruptionHelper {

    public static final Identifier VITIUM_ASPECT = AspectsLib.identifier("vitium");

    public static void addCorruption(ServerWorld world, BlockPos pos, int amount) {

        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Optional<RegistryKey<Biome>> optionalKey = biomeEntry.getKey();
        if (optionalKey.isEmpty()) {
            return;
        }

        CorruptionDataManager.modifyChunkAspect(world, new ChunkPos(pos), optionalKey.get().getValue(), VITIUM_ASPECT, amount);
    }
}