@file:Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")

package org.c0nstexpr.fishology

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.UseRodEvent

abstract class AbstractFishologyAction(val arg: UseRodEvent.Arg) {
    val item = arg.item
    val hand = arg.hand

    protected fun onCaughtFish(arg: CaughtFishEvent.Arg) {
        val bobber = arg.bobber
        val client = MinecraftClient.getInstance()
        val player = client.player.let {
            when (it?.uuid) {
                null -> return
                bobber.playerOwner?.uuid -> it
                else -> return
            }
        }

        if (player.getStackInHand(hand)?.isOf(item) == true || !arg.caught) return

        client.interactionManager?.interactItem(player, hand)

        reuseRod(client, player)
    }

    private fun reuseRod(client: MinecraftClient, player: ClientPlayerEntity) {
        // TODO: when to rethrow rod?
        client.interactionManager?.interactItem(player, hand)
    }
}
