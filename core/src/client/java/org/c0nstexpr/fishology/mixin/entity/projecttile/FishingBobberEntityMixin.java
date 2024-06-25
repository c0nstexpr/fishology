package org.c0nstexpr.fishology.mixin.entity.projecttile;

import net.minecraft.client.*;
import net.minecraft.entity.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.projectile.*;
import org.c0nstexpr.fishology.events.*;
import org.c0nstexpr.fishology.log.*;
import org.objectweb.asm.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

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

        var id = Optional.ofNullable(MinecraftClient.getInstance())
                     .map(c -> c.player)
                     .map(p -> p.fishHook)
                     .map(Entity::getId);

        if (id.isEmpty() || id.get() != bobber.getId()) return;

        if (CAUGHT_FISH.equals(trackedData))
            CaughtFishEvent.subject.onNext(new CaughtFishEvent.Arg(bobber, caughtFish));
        else if (HOOK_ENTITY_ID.equals(trackedData))
            HookedEvent.subject.onNext(new HookedEvent.Arg(bobber, hookedEntity));
    }
}
