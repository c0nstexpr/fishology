package org.c0nstexpr.fishology.mixin.client.network;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.c0nstexpr.fishology.events.ItemEntityVelPacketEvent;
import org.c0nstexpr.fishology.events.SlotUpdateEvent;
import org.c0nstexpr.fishology.events.SlotUpdateEvent.Arg;
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
        if (!(world.getEntityById(packet.getId()) instanceof ItemEntity item)) return;

        ItemEntityVelPacketEvent.subject.onNext(new ItemEntityVelPacketEvent.Arg(item));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(
            ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (packet.getSyncId() != 0) return;

        SlotUpdateEvent.subject.onNext(new Arg(packet.getSlot(), packet.getItemStack()));
    }
}
