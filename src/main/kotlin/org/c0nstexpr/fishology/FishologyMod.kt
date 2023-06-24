package org.c0nstexpr.fishology

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

@Suppress("unused")
class FishologyMod : ClientModInitializer {
    override fun onInitializeClient() {
        UseItemCallback.EVENT.register(
            UseItemCallback { player: PlayerEntity, world: World, hand: Hand ->
                val stack: ItemStack = player.getStackInHand(hand)
                if (stack.item is FishItem) {
                    return@UseItemCallback TypedActionResult.pass(stack)
                }
                return ActionResult.PASS
            }
        )
    }
}
