@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import org.c0nstexpr.fishology.core.events.CaughtFishEvent

class FishingInteraction(val rod: RodInteraction) : DisposableScope by DisposableScope() {
    init {
        CaughtFishEvent.observable.filter {
            it.run {
                (rod.player?.let { p -> p.uuid == bobber.playerOwner?.uuid } == true) && caught
            }
        }.subscribeScoped { onCaughtFish() }
    }

    private fun onCaughtFish() = rod.use { success ->
        if (!success) return@use
        rod.use()
    }
}
