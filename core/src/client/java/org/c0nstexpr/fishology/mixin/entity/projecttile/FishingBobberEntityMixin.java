package org.c0nstexpr.fishology.mixin.entity.projecttile;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.c0nstexpr.fishology.events.BobberOwnedEvent;
import org.c0nstexpr.fishology.events.CaughtFishEvent;
import org.c0nstexpr.fishology.events.HookedEvent;
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

        if (!bobber.getWorld().isClient) return;

        if (CAUGHT_FISH.equals(trackedData))
            CaughtFishEvent.subject.onNext(new CaughtFishEvent.Arg(bobber, caughtFish));
        else if (HOOK_ENTITY_ID.equals(trackedData))
            HookedEvent.subject.onNext(new HookedEvent.Arg(bobber, hookedEntity));
    }

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void setOwner(Entity entity, CallbackInfo ci) {
        final var bobber = (FishingBobberEntity) (Object) this;

        if (!(bobber.getWorld().isClient && entity instanceof ClientPlayerEntity player)) return;

        BobberOwnedEvent.subject.onNext(new BobberOwnedEvent.Arg(bobber, player));
    }
}
