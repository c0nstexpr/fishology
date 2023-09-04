package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.base.exceptions.TimeoutException
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.timeout
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import org.c0nstexpr.fishology.events.BobberOwnedEvent
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.ObservableStep
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.observableStep
import org.c0nstexpr.fishology.utils.swapHand
import kotlin.time.Duration.Companion.seconds

class AutoFishing(
    private val rod: Rod,
    private val caughtItem: Observable<ItemEntity?>,
) : SwitchDisposable() {
    private val playerId
        get() = rod.player.let {
            if (it == null) {
                logger.w("player is null")
                null
            } else {
                it.id
            }
        }

    private val recastSub = PublishSubject<RodItem>()

    override fun onEnable() = disposableScope {
        caughtItem.filter { it == null }
            .tryOn()
            .subscribeScoped {
                logger.d("retrieve rod")
                rod.use()
            }

        observableStep(observeCaughtItem())
            .tryOn { _, e -> onRetry(e) }
            .subscribeScoped { }

        observableStep(recastSub).switch({ observeHooked() })
            .tryOn()
            .subscribeScoped { }
    }

    private fun RodItem.observeHooked(): Observable<SelectedSlotUpdateEvent.Arg> {
        var tryCount = 0
        val handler = rod.client.networkHandler
        if (handler == null) {
            logger.w("network handler is null")

            return observableOfEmpty()
        }

        return observableStep(BobberOwnedEvent.observable.filter { it.player.id == playerId })
            .switchMaybe(
                {
                    HookedEvent.observable.filter { it.bobber.id == bobber.id }
                        .firstOrComplete()
                        .map { Pair(player, player.inventory.selectedSlot) }
                }) {
                handler.swapHand()
                handler.swapHand()
            }
            .switchMaybe(
                {
                    SelectedSlotUpdateEvent.observable.filter {
                        first.fishHook == null && it.slot == second
                    }
                        .firstOrComplete()
                }) {
                ++tryCount
                logger.d("attempt to recast rod($tryCount)")
                rod.use()
            }
    }

    private fun observeCaughtItem(): ObservableStep<Any> {
        fun Entity?.isHigher(itemY: Double): Boolean {
            return (this?.pos?.y ?: return false) - itemY >= 0.01
        }

        return observableStep(caughtItem.notNull())
            .switchMaybe(
                {
                    fun isSameItem(it: ItemEntity) = it.id == id

                    merge(
                        ItemEntityVelEvent.observable.map { it.entity }
                            .filter(::isSameItem)
                            .filter { velocity.y <= 0.0 && rod.player.isHigher(pos.y) },
                        ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem),
                    )
                        .firstOrComplete()
                        .timeout(timeout, ioScheduler)
                }) {
                logger.d("recast rod")
                if (rod.use()) {
                    rod.rodItem.let {
                        if (it == null) {
                            logger.w("missed rod item")
                        } else {
                            recastSub.onNext(it)
                        }
                    }
                } else {
                    logger.d("recast not success")
                }
            }
    }

    private fun onRetry(it: Throwable): Boolean {
        if (it !is TimeoutException) {
            return false
        }

        logger.w("Recast action has taken longer than $timeout")

        return true
    }

    companion object {
        private val timeout = 3.seconds
    }
}
