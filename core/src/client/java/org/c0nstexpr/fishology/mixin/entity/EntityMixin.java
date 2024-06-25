package org.c0nstexpr.fishology.mixin.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.c0nstexpr.fishology.events.*;
import org.c0nstexpr.fishology.log.*;
import org.objectweb.asm.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(Entity.class)
abstract class EntityMixin {
    @Shadow private World world;
    @Shadow private boolean onGround;

    @Inject(method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
    private void setVelocity(Vec3d vec3d, CallbackInfo ci) {
        final var entity = (Entity) (Object) this;

        if (!(world.isClient && entity instanceof ItemEntity item)) return;

        ItemEntityVelEvent.subject.onNext(new ItemEntityVelEvent.Arg(item));
    }

    @Inject(method = "onRemoved", at = @At("TAIL"))
    private void onRemoved(CallbackInfo ci) {
        final var entity = (Entity) (Object) this;

        if (!world.isClient) return;

        EntityRemoveEvent.subject.onNext(new EntityRemoveEvent.Arg(entity));
    }

    @Inject(
        method = {"setOnGround(Z)V", "setOnGround(ZLnet/minecraft/util/math/Vec3d;)V"},
        at = @At("TAIL"))
    private void setOnGround(CallbackInfo ci) {
        final var entity = (Entity) (Object) this;

        if (!EntityExtKt.getInClient(entity)) return;

        EntityOnGroundEvent.subject.onNext(new EntityOnGroundEvent.Arg(entity, onGround));
    }
}
