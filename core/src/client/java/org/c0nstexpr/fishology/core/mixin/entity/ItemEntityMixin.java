package org.c0nstexpr.fishology.core.mixin.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V",
            at = @At("TAIL"))
    private void onInit(EntityType<? extends ItemEntity> entityType, World world, CallbackInfo ci) {
        final var itemEntity = (ItemEntity) (Object) this;

        var stack = itemEntity.getStack();
    }
}
