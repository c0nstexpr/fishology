@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.subscribe
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.utils.Disposed

class FishingInteraction(val rod: RodInteraction) : Disposable {
    private val hook get() = rod.player?.fishHook

    private val rodUseSubscription = rod.afterUse.subscribe {
        if (hook != null) caughtFishSubscription.dispose()
        else if (caughtFishSubscription.isDisposed) caughtFishSubscription =
            CaughtFishEvent.observable.subscribe(onNext = ::onCaughtFish)
    }

    private var caughtFishSubscription = Disposed()

    private fun onCaughtFish(arg: CaughtFishEvent.Arg) {
        rod.run {
            player.run{
                if (
                    this == null ||
                    uuid != arg.bobber.playerOwner?.uuid ||
                    !arg.caught
                ) return

                if (getStackInHand(hand)?.isOf(item) == false)  // filter out other items in hand
                {
                    caughtFishSubscription.dispose()
                    return
                }
            }
        }

        reuseRod()
    }

    // TODO: when to rethrow rod?
    private fun reuseRod() = rod.use()

    override val isDisposed: Boolean get() = rodUseSubscription.isDisposed

    override fun dispose() {
        rodUseSubscription.dispose()
        caughtFishSubscription?.dispose()
    }
}
