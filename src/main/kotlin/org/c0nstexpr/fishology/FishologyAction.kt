@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.UseRodEvent

class FishologyAction(val client: MinecraftClient, val arg: UseRodEvent.Arg) : Disposable {
    private val scope = CaughtFishEvent.observable.subscribe(onNext = ::onCaughtFish)

    val item = arg.item
    val hand = arg.hand

    private fun onCaughtFish(arg: CaughtFishEvent.Arg) {
        val bobber = arg.bobber
        val player = client.player.let {
            when (it?.uuid) {
                null -> return
                bobber.playerOwner?.uuid -> it
                else -> return
            }
        }

        if (player.getStackInHand(hand)?.isOf(item) == true || !arg.caught) return

        client.interactionManager?.interactItem(player, hand)

        reuseRod(player)
    }

    var reusingRod: Boolean = false
        private set

    private fun reuseRod(player: ClientPlayerEntity) {
        // TODO: when to rethrow rod?

        reusingRod = true
        client.interactionManager?.interactItem(player, hand)
        reusingRod = false
    }

    override val isDisposed: Boolean
        get() = scope.isDisposed

    override fun dispose() {
        scope.dispose()
    }
}
