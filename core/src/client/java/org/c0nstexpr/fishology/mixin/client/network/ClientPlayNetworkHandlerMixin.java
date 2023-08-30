package org.c0nstexpr.fishology.mixin.client.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import org.c0nstexpr.fishology.events.ItemEntityVelPacketEvent;
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent;
import org.c0nstexpr.fishology.events.SlotUpdateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;

    @Final @Shadow private MinecraftClient client;

    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (!(world.getEntityById(packet.getId()) instanceof ItemEntity item)) return;

        ItemEntityVelPacketEvent.subject.onNext(new ItemEntityVelPacketEvent.Arg(item));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(
            ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        SlotUpdateEvent.subject.onNext(
                new SlotUpdateEvent.Arg(
                        packet.getSlot(), packet.getItemStack(), packet.getSyncId()));
    }

    @Inject(method = "onUpdateSelectedSlot", at = @At("TAIL"))
    private void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet, CallbackInfo ci) {
        SelectedSlotUpdateEvent.subject.onNext(new SelectedSlotUpdateEvent.Arg(packet.getSlot()));
    }
}
