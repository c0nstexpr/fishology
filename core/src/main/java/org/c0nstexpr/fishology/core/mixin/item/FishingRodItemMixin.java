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

@SuppressWarnings("DataFlowIssue")
@Mixin(FishingRodItem.class)
class FishingRodItemMixin {
    @Inject(method = "use", at = @At("TAIL"))
    private void afterUse(
            World world,
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        UseRodEvent.afterSubject.onNext(
                new UseRodEvent.Arg((FishingRodItem) (Object) this, hand, player));
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void beforeUse(
            World world,
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        UseRodEvent.beforeSubject.onNext(
                new UseRodEvent.Arg((FishingRodItem) (Object) this, hand, player));
    }
}
