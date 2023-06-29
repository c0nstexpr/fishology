package org.c0nstexpr.fishology.mixin.entity.projecttile;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.c0nstexpr.fishology.events.BobberStateChangeEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    private static final String ByteCodeName =
            "Lnet/minecraft/entity/projectile/FishingBobberEntity";

    @Shadow private FishingBobberEntity.State state;

    @Inject(
            method = "tick",
            at =
                    @At(
                            value = "FIELD",
                            target = ByteCodeName + ";" + "state:" + ByteCodeName + "$State;"))
    private void tick(CallbackInfo ci) {
        BobberStateChangeEvents.EVENT.invoker().change((FishingBobberEntity) (Object) this, state);
    }
}
