package org.c0nstexpr.fishology

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.item.FishingRodItem
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.events.BobberStateChangeEvents

@Suppress("MemberVisibilityCanBePrivate")
class FishologyAction(hand: Hand, item: FishingRodItem) {
    var item: FishingRodItem = item
        private set
    var hand: Hand = hand
        private set

    companion object {

    }

    private fun onBobberStateChange(
        bobber: FishingBobberEntity,
        state: FishingBobberEntity.State
    ) {
        val client = MinecraftClient.getInstance()
        val player = client.player

        if (bobber.playerOwner?.uuid != player?.uuid || state != FishingBobberEntity.State.HOOKED_IN_ENTITY) return


        client.interactionManager?.interactItem(bobber.playerOwner, bobber.owner)
    }
}
