package org.c0nstexpr.fishology.mixin.client.network;

import net.minecraft.client.network.*;
import net.minecraft.client.world.*;
import net.minecraft.entity.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.*;
import org.c0nstexpr.fishology.events.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ClientPlayNetworkHandler.class) abstract class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        SlotUpdateEvent.subject.onNext(
            new SlotUpdateEvent.Arg(packet.getSlot(), packet.getStack(), packet.getSyncId()));
    }

    @Inject(method = "onUpdateSelectedSlot", at = @At("TAIL"))
    private void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet, CallbackInfo ci) {
        SelectedSlotUpdateEvent.subject.onNext(new SelectedSlotUpdateEvent.Arg(packet.getSlot()));
    }

    @Inject(method = "onEntitySpawn", at = @At("TAIL"))
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (!(world.getEntityById(packet.getId()) instanceof ItemEntity item)) return;

        ItemEntitySpawnEvent.subject.onNext(new ItemEntitySpawnEvent.Arg(
            item,
            new Vec3d(packet.getX(), packet.getY(), packet.getZ()),
            new Vec3d(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ())));
    }

    @Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
    private void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (!(world.getEntityById(packet.id()) instanceof ItemEntity item)) return;

        ItemEntityTrackerEvent.subject.onNext(new ItemEntityTrackerEvent.Arg(item));
    }
}
