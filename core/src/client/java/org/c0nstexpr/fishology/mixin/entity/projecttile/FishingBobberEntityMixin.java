package org.c0nstexpr.fishology.mixin.entity.projecttile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.c0nstexpr.fishology.events.*;
import org.c0nstexpr.fishology.log.EntityExtKt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(FishingBobberEntity.class)
abstract class FishingBobberEntityMixin {
    @Final @Shadow private static TrackedData<Boolean> CAUGHT_FISH;
    @Final @Shadow private static TrackedData<Boolean> HOOK_ENTITY_ID;

    @Shadow private boolean caughtFish;
    @Shadow private Entity hookedEntity;

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void onTrackedDataSet(TrackedData<?> trackedData, CallbackInfo ci) {
        final var bobber = (FishingBobberEntity) (Object) this;

        if (!EntityExtKt.getInClient(bobber)) return;

        if (CAUGHT_FISH.equals(trackedData))
            CaughtFishEvent.subject.onNext(new CaughtFishEvent.Arg(bobber, caughtFish));
        else if (HOOK_ENTITY_ID.equals(trackedData))
            HookedEvent.subject.onNext(new HookedEvent.Arg(bobber, hookedEntity));
    }
}
