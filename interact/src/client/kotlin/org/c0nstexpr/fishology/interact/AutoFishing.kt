package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.base.exceptions.TimeoutException
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.maybe.timeout
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.maybeStep
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

    private val recastSub = PublishSubject<Recast>()

    private val interruptedSub = PublishSubject<Unit>()

    override fun onEnable() = disposableScope {
        caughtItem.filter { it == null }
            .tryOn()
            .subscribeScoped {
                logger.d("retrieve rod")
                rod.use()
            }

        observeCaughtItem().tryOn { _, e -> onRetry(e) }.subscribeScoped { }

        observableStep(recastSub).switchMaybe({ observeRecast() }).tryOn().subscribeScoped { }
    }

    private fun Recast.observeRecast(): Maybe<SelectedSlotUpdateEvent.Arg> {
        var tryCount = 0
        val handler = rod.client.networkHandler
        if (handler == null) {
            logger.w("network handler is null")

            return maybeOfEmpty()
        }

        return maybeStep(
            HookedEvent.observable.filter { it.bobber.id == rodItem.player.fishHook?.id }
                .map { rodItem.player.inventory.selectedSlot }
                .firstOrComplete(),
        ) {
            handler.swapHand()
            handler.swapHand()
        }
            .flat(
                {
                    SelectedSlotUpdateEvent.observable.filter { it.slot == this }.firstOrComplete()
                },
            ) {
                ++tryCount
                logger.d("attempt to recast rod($tryCount)")
                recast(count + 1)
            }
    }

    private fun observeCaughtItem() = observableStep(caughtItem.notNull())
        .switchMaybe(
            {
                fun isSameItem(it: ItemEntity) = it.id == id

                merge(
                    ItemEntityVelEvent.observable.map { it.entity }
                        .filter(::isSameItem)
                        .filter {
                            val playerPos = rod.player?.pos ?: return@filter false

                            vecComponents.any { it(velocity) <= 0.0 && it(playerPos) - it(pos) > 0.01 }
                        },
                    ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem),
                )
                    .firstOrComplete()
                    .timeout(timeout, ioScheduler)
            },
        ) {
            logger.d("recast rod")
            recast()
        }

    private fun recast(count: Int = 0) {
        if (rod.use()) {
            rod.rodItem.let {
                if (it == null) {
                    logger.w("missed rod item")
                } else {
                    recastSub.onNext(Recast(it, count))
                }
            }
        } else {
            logger.d("recast not success")
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

        private data class Recast(val rodItem: RodItem, val count: Int)

        private val vecComponents = arrayOf(Vec3d::x, Vec3d::y, Vec3d::z)
    }
}
