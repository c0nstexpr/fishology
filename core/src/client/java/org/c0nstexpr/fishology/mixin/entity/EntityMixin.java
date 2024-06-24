package org.c0nstexpr.fishology.mixin.entity;

import net.minecraft.entity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.c0nstexpr.fishology.events.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private World world;

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
}
