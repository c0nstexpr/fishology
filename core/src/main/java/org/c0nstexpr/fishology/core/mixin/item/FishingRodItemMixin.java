package org.c0nstexpr.fishology.core.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.c0nstexpr.fishology.core.events.UseRodEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
class FishingRodItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void beforeUse(
            World world,
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) return;

        UseRodEvent.beforeUseSubject.onNext(new UseRodEvent.Arg(hand, player));
    }
}
