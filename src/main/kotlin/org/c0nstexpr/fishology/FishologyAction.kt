package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.subscribe
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.UseRodEvent

class FishologyAction(arg: UseRodEvent.Arg) : AbstractFishologyAction(arg), Disposable {
    private val scope = CaughtFishEvent.observable.subscribe(onNext = ::onCaughtFish)

    override val isDisposed: Boolean
        get() = scope.isDisposed

    override fun dispose() {
        scope.dispose()
    }
}
