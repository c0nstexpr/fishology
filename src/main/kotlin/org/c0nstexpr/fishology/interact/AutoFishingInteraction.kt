package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.core.events.EntityFallingEvent
import org.c0nstexpr.fishology.core.events.EntityRemovedEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.addScope

class AutoFishingInteraction(
        val useRod: (callback: (Boolean) -> Unit) -> Unit
) : DisposableScope by DisposableScope() {
    private val cycleSubject = PublishSubject<Boolean>()
    val cycle: Observable<Boolean> = cycleSubject

    var hooked: Entity? = null
    var player: ClientPlayerEntity? = null

    init {
        CaughtFishEvent.observable.filter {
            it.run { (player?.fishHook?.uuid == bobber.uuid) && caught }
        }.subscribeScoped { onCaughtFish { cycleSubject.onNext(it) } }
    }

    private val tmpDisposable = CompositeDisposable()

    private fun onCaughtFish(callback: (Boolean) -> Unit) {
        logger.d("try to retrieve rod")

        useRod {
            if (it) onRecast(callback)
            else callback(false)
        }
    }

    private fun onRecast(callback: (Boolean) -> Unit) {
        logger.d("try to recast rod")

        val hookedId = hooked?.id ?: return callback(false)
        val recastObservable = merge(
                EntityFallingEvent.observable.map { it.entity },
                EntityRemovedEvent.observable.map { it.entity })
                .filter { it.id == hookedId }.firstOrComplete()

        tmpDisposable.addScope { d ->
            recastObservable.subscribe {
                d.get().dispose()
                logger.d("recast rod")
                useRod(callback)
            }
        }
    }
}
