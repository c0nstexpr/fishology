package org.c0nstexpr.fishology.mixin.item;

import net.minecraft.client.network.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.c0nstexpr.fishology.events.*;
import org.c0nstexpr.fishology.events.UseRodEvent.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(FishingRodItem.class) public abstract class FishingRodItemMixin {
    @Inject(method = "use", at = @At("HEAD")) private void use(
        @NotNull World world,
        PlayerEntity player,
        Hand hand,
        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!(world.isClient && player instanceof ClientPlayerEntity)) return;

        UseRodEvent.useSubject.onNext(new Arg(hand, player, player.fishHook == null));
    }
}
