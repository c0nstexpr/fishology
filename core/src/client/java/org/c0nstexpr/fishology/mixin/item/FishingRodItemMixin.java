package org.c0nstexpr.fishology.mixin.item;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.c0nstexpr.fishology.events.UseRodEvent;
import org.c0nstexpr.fishology.events.UseRodEvent.Arg;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void use(
            @NotNull World world,
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!(world.isClient && player instanceof ClientPlayerEntity)) return;

        UseRodEvent.useSubject.onNext(new Arg(hand, player, player.fishHook == null));
    }
}
