package org.c0nstexpr.fishology.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.c0nstexpr.fishology.events.UseRodEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
abstract class FishingRodItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void beforeUse(
            @NotNull World world,
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) return;

        UseRodEvent.beforeUseSubject.onNext(new UseRodEvent.Arg(hand, player));
    }

    @Inject(method = "use", at = @At("TAIL"))
    private void afterUse(
            @NotNull World world,
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) return;

        UseRodEvent.afterUseSubject.onNext(new UseRodEvent.Arg(hand, player));
    }
}
