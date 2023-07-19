package org.c0nstexpr.fishology.core.mixin.entity.player;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import org.c0nstexpr.fishology.core.events.ItemCoolDownEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCooldownManager.class)
public class ItemCooldownManagerMixin {
    @Inject(method = "remove", at = @At("TAIL"))
    private void onTrackedDataSet(Item item, CallbackInfo ci) {
        ItemCoolDownEvent.subject.onNext(new ItemCoolDownEvent.Arg(item));
    }
}
