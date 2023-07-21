package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.logger

class AutoFishingInteraction(val rod: RodInteraction, val hooked: HookedInteraction) : DisposableScope by DisposableScope() {
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
            onCast()
        }
    }

    private fun onCast(){
        logger.debug("rod retrieved, recast rod")

        val bobberY = rod.player?.eyeY

        val entity = hooked.entity
        if(entity?.velocity?.y?.compareTo(0) == -1 && entity?.y < bobberY)
        {
            logger.debug("previous hooked entity is falling, recast rod")
            rod.use()
            return
        }

        rod.use()
    }
}
