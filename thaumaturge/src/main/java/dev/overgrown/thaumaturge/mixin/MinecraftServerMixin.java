package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.block.vessel.AspectReactionHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.*;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements AspectReactionHolder.Provider {

    @Mutable
    @Final @Shadow protected final SaveProperties saveProperties;

    @Unique
    private AspectReactionHolder holder;

    protected MinecraftServerMixin(SaveProperties saveProperties) {
        this.saveProperties = saveProperties;
        throw new IllegalStateException("No Object should be constructed from the mixin");
    }


    @Override
    public AspectReactionHolder thaumaturge$getAspectReactionHolder() {
        if (this.holder == null) {
            this.holder = new AspectReactionHolder(saveProperties.getGeneratorOptions().getSeed());
        }
        return this.holder;
    }
}
