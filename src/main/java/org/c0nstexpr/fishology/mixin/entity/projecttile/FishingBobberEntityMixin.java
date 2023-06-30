package org.c0nstexpr.fishology.mixin.entity.projecttile;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.MathHelper;
import org.c0nstexpr.fishology.events.BobberStateChangeEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    private static final String ByteCodeName =
            "Lnet/minecraft/entity/projectile/FishingBobberEntity";

    @Final @Shadow private static TrackedData<Boolean> CAUGHT_FISH;

    @Shadow private boolean caughtFish;

    @Shadow private FishingBobberEntity.State state;

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void onTrackedDataSet(TrackedData<?> trackedData, CallbackInfo ci) {
        if (CAUGHT_FISH.equals(trackedData)) {}
    }
}
