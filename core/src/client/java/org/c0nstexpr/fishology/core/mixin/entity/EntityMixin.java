package org.c0nstexpr.fishology.core.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.c0nstexpr.fishology.core.events.EntityFallingEvent;
import org.c0nstexpr.fishology.core.events.EntityRemovedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
    private void setVelocity(Vec3d vec3d, CallbackInfo ci) {
        final var entity = (Entity) (Object) this;

        if (vec3d.y < 0) EntityFallingEvent.subject.onNext(new EntityFallingEvent.Arg(entity));
    }

    @Inject(method = "onRemoved", at = @At("TAIL"))
    private void onRemoved(CallbackInfo ci) {
        final var entity = (Entity) (Object) this;

        EntityRemovedEvent.subject.onNext(new EntityRemovedEvent.Arg(entity));
    }
}
