@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.utils.coroutineScope

class FishingInteraction(val rod: RodInteraction) : DisposableScope by DisposableScope() {
    val client get() = rod.client

    val caughtFishFlow = CaughtFishEvent.flow.filter {
        it.run {
            (rod.player?.let { p -> p.uuid == bobber.playerOwner?.uuid } == true) && caught
        }
    }

    val interruptionFlow = rod.beforeUseFlow.filter { state == State.Idle }

    init {
        val job = client.coroutineScope.launch { caughtFishFlow.collect { onCaughtFish() } }
        doOnDispose(job::cancel)
    }

    enum class State {
        Idle,
        Retrieving,
        Recasting
    }

    var state = State.Idle
        private set

    private suspend fun onCaughtFish() = rod.run {
        state = State.Retrieving
        rod.use()
        state = State.Recasting
        rod.use()
        state = State.Idle
    }
}
