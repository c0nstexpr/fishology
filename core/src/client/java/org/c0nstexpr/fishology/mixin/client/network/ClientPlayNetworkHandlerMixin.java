package org.c0nstexpr.fishology.mixin.client.network;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.c0nstexpr.fishology.events.ItemEntitySetVelocityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;

    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        Entity entity = world.getEntityById(packet.getId());

        if (entity instanceof ItemEntity item)
            ItemEntitySetVelocityEvent.subject.onNext(new ItemEntitySetVelocityEvent.Arg(item));
    }
}
