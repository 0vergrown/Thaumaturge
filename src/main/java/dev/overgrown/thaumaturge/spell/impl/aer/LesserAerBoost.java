package dev.overgrown.thaumaturge.spell.impl.aer;

import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

public class LesserAerBoost implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("lesser_aer_boost");

    @Override
    public void execute(ServerPlayerEntity player) {
        player.addVelocity(0, 0.9, 0);
        player.velocityModified = true;
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Spawn wind charge particles at the player's feet
        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL,
                player.getX(), player.getY(), player.getZ(),1,0.5,0.1, 0.5, 0.1
        );
    }
}