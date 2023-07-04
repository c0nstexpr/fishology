@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.subscribe
import org.c0nstexpr.fishology.config.FishologyConfigModel
import org.c0nstexpr.fishology.config.initObserve
import org.c0nstexpr.fishology.events.UseRodEvent

class Fishology : Disposable {
    private var action: FishologyAction? = null

    private var subscription: Disposable? = null

    val config: FishologyConfig = FishologyConfig.createAndLoad()

    init {
        config.initObserve(FishologyConfigModel::enabled) {
            if (!it) return@initObserve

            subscription?.dispose()
            subscription = UseRodEvent.observable.subscribe(onNext = ::onUseRod)
        }
    }

    private fun onUseRod(arg: UseRodEvent.Arg) {
        action.let {
            if (it != null) {
                it.dispose()
                if (it.arg == arg) return@let
            }
        }

        action = FishologyAction(arg)
    }

    override val isDisposed: Boolean get() = subscription?.isDisposed ?: true

    override fun dispose() {
        subscription?.dispose()
        action?.dispose()
    }
}
