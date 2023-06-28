package org.c0nstexpr.fishology.mixin.entity.projecttile;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    @Shadow private FishingBobberEntity.State state;

    @Inject(
            method = "tick",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/entity/projectile/FishingBobberEntity;state:Lnet/minecraft/entity/projectile/FishingBobberEntity$State;"))
    private void tick(CallbackInfo ci) {}
}
