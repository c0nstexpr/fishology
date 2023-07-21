package org.c0nstexpr.fishology.core.mixin.entity.projecttile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.c0nstexpr.fishology.core.FishologyCoreModKt;
import org.c0nstexpr.fishology.core.events.CaughtFishEvent;
import org.c0nstexpr.fishology.core.events.HookedEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(FishingBobberEntity.class)
class FishingBobberEntityMixin {
    @Final @Shadow private static TrackedData<Boolean> CAUGHT_FISH;
    @Final @Shadow private static TrackedData<Boolean> HOOK_ENTITY_ID;

    @Shadow private boolean caughtFish;
    @Shadow private Entity hookedEntity;

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void onTrackedDataSet(TrackedData<?> trackedData, CallbackInfo ci) {
        final FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;

        if (CAUGHT_FISH.equals(trackedData)) {
            FishologyCoreModKt.logger.debug("detected caught fish in mixin");
            CaughtFishEvent.subject.onNext(new CaughtFishEvent.Arg(bobber, caughtFish));
        } else if (HOOK_ENTITY_ID.equals(trackedData)) {
            FishologyCoreModKt.logger.debug("detected hooked entity in mixin");
            HookedEvent.subject.onNext(new HookedEvent.Arg(bobber, hookedEntity));
        }
    }
}
