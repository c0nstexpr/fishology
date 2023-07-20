package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.logger

class AutoFishingInteraction(val rod: RodInteraction) : DisposableScope by DisposableScope() {
    init {
        logger.debug("Initializing fishing interaction")

        CaughtFishEvent.observable.filter {
            it.run {
                (rod.player?.let { p -> p.uuid == bobber.playerOwner?.uuid } == true) && caught
            }
        }.subscribeScoped { onCaughtFish() }
    }

    private fun onCaughtFish() {
        logger.debug("detected fish was caught, retrieving rod")

        rod.use { success ->
            if (!success) return@use

            logger.debug("rod retrieved, recast rod")
            rod.use()
        }
    }
}
