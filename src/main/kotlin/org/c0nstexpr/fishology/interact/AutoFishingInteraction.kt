package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.single
import com.badoo.reaktive.single.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import org.c0nstexpr.fishology.core.events.CaughtFishEvent
import org.c0nstexpr.fishology.core.events.EntityFallingEvent
import org.c0nstexpr.fishology.core.events.EntityRemovedEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.onNextComplete

class AutoFishingInteraction(
    var player: Entity,
    val useRod: () -> Single<Boolean>,
    var hooked: Entity
) : DisposableScope by DisposableScope() {
    private val cycleSubject = PublishSubject<Boolean>()

    val cycle: Observable<Boolean> = cycleSubject

    init {
        val serialDisposable = SerialDisposable()

        CaughtFishEvent.observable.filter {
            it.run { (player.uuid == bobber.playerOwner?.uuid) && caught }
        }.subscribeScoped {
            serialDisposable.set(onCaughtFish().subscribe { cycleSubject.onNext(it) })
        }
    }

    private fun onCaughtFish(): Single<Boolean> {
        logger.d("try to retrieve rod")

        return single { emitter ->
            emitter.setDisposable(
                DisposableScope().apply {
                    useRod().subscribeScoped {
                        if (it) onRecast().subscribeScoped { emitter.onSuccess(it) }
                        else emitter.onSuccess(false)
                    }
                }
            )
        }
    }

    private fun onRecast(): Single<Boolean> {
        logger.d("try to recast rod")

        val subject = BehaviorSubject(false)
        lateinit var disposable: Disposable

        disposable =
            merge(
                EntityFallingEvent.observable.filter { it.entity.id == hooked.id },
                EntityRemovedEvent.observable.filter { it.entity.id == hooked.id })
                .firstOrComplete()
                .subscribeScoped {
                    disposable.dispose()

                    lateinit var disposableInner: Disposable

                    disposableInner = useRod().subscribe {
                        subject.onNextComplete(it)
                        disposableInner.dispose()
                    }
                }

        return subject.filter { it }.firstOrDefault(false)
    }
}
