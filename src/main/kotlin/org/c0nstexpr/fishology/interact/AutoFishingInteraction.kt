package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.toMaybe
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.single.*
import net.minecraft.entity.Entity
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.core.events.EntityFallingEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.asSingle

class AutoFishingInteraction(
    var player: Entity,
    val useRod: () -> Single<Unit>,
    var hooked: Entity
) {
    val caughtFish = run {
        val retrieveDisposable = SerialDisposable()

        CaughtFishEvent.observable.filter {
            it.run { (this@AutoFishingInteraction.player.uuid == bobber.playerOwner?.uuid) && caught }
        }
            .map {
                logger.d("detected caught")
                retrieveDisposable.set(onCaughtFish().subscribe())
            }
            .doOnBeforeDispose { retrieveDisposable.dispose() }
    }

    private fun onCaughtFish(): Single<Unit> {
        logger.d("retrieving rod")
        val recastDisposable = SerialDisposable()

        return useRod().map { recastDisposable.set(onRecast().subscribe()) }
            .doOnBeforeDispose { recastDisposable.dispose() }
    }

    private fun onRecast(): Single<Unit> {
        logger.d("wait for recasting rod")

        return EntityFallingEvent.observable.filter { it.entity.id == hooked.id }.any().map {
            logger.d("hooked entity is falling, recast rod")
            useRod()
        }
    }
}
