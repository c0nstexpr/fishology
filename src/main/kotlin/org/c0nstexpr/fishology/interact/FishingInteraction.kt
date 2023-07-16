@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import org.c0nstexpr.fishology.core.events.CaughtFishEvent

class FishingInteraction(val rod: RodInteraction) : DisposableScope by DisposableScope() {


    init {
        CaughtFishEvent.observable.subscribeScoped(onNext = ::onCaughtFish)
        rod.beforeUse.subscribeScoped {

        }
    }

    private var caughtFish = false

    private fun onCaughtFish(arg: CaughtFishEvent.Arg) = rod.run {
        if (
            player?.uuid != arg.bobber.playerOwner?.uuid ||
            !arg.caught
        ) return@run

        caughtFish = true

        use()
        use()
    }
}
