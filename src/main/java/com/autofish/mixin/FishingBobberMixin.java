package com.autofish.mixin;

import com.autofish.AutoFishHook;
import com.autofish.AutoFishMod;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingBobberMixin {

    @Shadow private int nibble;

    private int prevNibble = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!AutoFishMod.enabled) return;
        // nibble goes from 0 to positive when fish bites
        if (prevNibble == 0 && nibble > 0) {
            AutoFishHook.hasBite = true;
        }
        prevNibble = nibble;
    }
}
