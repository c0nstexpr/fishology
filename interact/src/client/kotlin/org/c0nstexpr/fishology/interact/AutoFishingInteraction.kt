package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.EntityFallingEvent
import org.c0nstexpr.fishology.events.EntityRemovedEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.addScope
import java.util.UUID

class AutoFishingInteraction(
    val useRod: (callback: (Boolean) -> Unit) -> Unit,
    var uuid: UUID,
    var hooked: BehaviorObservable<Entity?>,
) : DisposableScope by DisposableScope() {
    private val cycleSubject = PublishSubject<Boolean>()
    val cycle: Observable<Boolean> = cycleSubject

    init {
        CaughtFishEvent.observable.filter {
            it.run { (uuid == bobber.playerOwner?.uuid) && caught }
        }.subscribeScoped { onCaughtFish { cycleSubject.onNext(it) } }
    }

    private val tmpDisposable = CompositeDisposable()

    private fun onCaughtFish(callback: (Boolean) -> Unit) {
        logger.d("try to retrieve rod")

        val preHook = hooked.value?.uuid

        useRod {
            if (it) {
                onRecast(callback, preHook)
            } else {
                callback(false)
            }
        }
    }

    private fun onRecast(callback: (Boolean) -> Unit, preHook: UUID?) {
        logger.d("try to recast rod")

        val hookObservable = hooked.notNull()
            .filter { it.uuid != preHook }
            .firstOrComplete()

        tmpDisposable.addScope(
            hookObservable.subscribe { hook ->
                val recastObservable = merge(
                    EntityFallingEvent.observable.map { it.entity },
                    EntityRemovedEvent.observable.map { it.entity },
                )
                    .filter { it.uuid == hook.uuid }.firstOrComplete()

                tmpDisposable.addScope { d ->
                    recastObservable.subscribe {
                        d.get().dispose()
                        logger.d("recast rod")
                        useRod(callback)
                    }
                }
            },
        )
    }
}
