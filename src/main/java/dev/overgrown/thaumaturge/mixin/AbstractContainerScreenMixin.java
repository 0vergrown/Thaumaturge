package dev.overgrown.thaumaturge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HandledScreen.class)
@Debug(export = true)
public class AbstractContainerScreenMixin {

    @ModifyVariable(method = "mouseClicked",at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isClickOutsideBounds(DDIII)Z",shift = At.Shift.BY,by = 2),ordinal = 1)
    private boolean fixBug(boolean o, @Local Slot clicked) {
        return clicked == null && o;
    }
}
