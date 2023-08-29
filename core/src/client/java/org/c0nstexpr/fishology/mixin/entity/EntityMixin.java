package org.c0nstexpr.fishology.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.c0nstexpr.fishology.events.ItemEntityRmovEvent;
import org.c0nstexpr.fishology.events.ItemEntityVelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(Entity.class)
abstract class EntityMixin {
    @Shadow private World world;

    @Inject(method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
    private void setVelocity(Vec3d vec3d, CallbackInfo ci) {
        final var entity = (Entity) (Object) this;

        if (!(world.isClient && entity instanceof ItemEntity item)) return;

        ItemEntityVelEvent.subject.onNext(new ItemEntityVelEvent.Arg(item));
    }

    @Inject(method = "onRemoved", at = @At("TAIL"))
    private void onRemoved(CallbackInfo ci) {
        if (!(world.isClient && (Entity) (Object) this instanceof ItemEntity item)) return;

        ItemEntityRmovEvent.subject.onNext(new ItemEntityRmovEvent.Arg(item));
    }
}
