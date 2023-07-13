@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.take
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.core.events.ItemCooldownEvent
import org.c0nstexpr.fishology.utils.Disposed

class FishingInteraction(val rod: RodInteraction) : DisposableScope by DisposableScope() {
    private var cooldownSubscription: Disposable = Disposable()

    init {
        var caughtFishSubscription = Disposed()

        rod.beforeUse.subscribeScoped {
            if (!cooldownSubscription.isDisposed) { // from user input?
                caughtFishSubscription.dispose()
                cooldownSubscription.dispose()
            } else if (caughtFishSubscription.isDisposed) caughtFishSubscription =
                CaughtFishEvent.observable.subscribe(onNext = ::onCaughtFish)
        }

        doOnDispose(caughtFishSubscription::dispose)
    }

    private fun onCaughtFish(arg: CaughtFishEvent.Arg) = rod.run {
        if (
            player?.uuid != arg.bobber.playerOwner?.uuid ||
            !arg.caught
        ) return@run

        prepareRecast()
        use() // retrieve

    }

    private fun prepareRecast() = rod.run {
        itemStack?.run {
            cooldownSubscription.dispose()
            cooldownSubscription = ItemCooldownEvent.observable.filter { isOf(it.item) }.take(1)
                .subscribeScoped {
                    if (equals(player?.getStackInHand(hand))) client.execute {
                        rod.use()
                    }
                }
        }
    }
}
