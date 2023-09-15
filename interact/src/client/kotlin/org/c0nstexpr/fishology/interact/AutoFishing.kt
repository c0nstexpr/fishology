package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.base.exceptions.TimeoutException
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.timeout
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.scheduler.ioScheduler
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.observableStep
import kotlin.time.Duration.Companion.seconds

class AutoFishing(
    private val rod: Rod,
    private val caughtItem: Observable<ItemEntity?>,
) : SwitchDisposable() {
    private val playerId
        get() = rod.player.let {
            if (it == null) {
                logger.w<AutoFishing> { "player is null" }
                null
            } else {
                it.id
            }
        }

    override fun onEnable() = disposableScope {
        caughtItem.filter { it == null }
            .tryOn()
            .subscribeScoped {
                logger.d<AutoFishing> { "retrieve rod" }
                rod.use()
            }

        observeCaughtItem().tryOn { _, e -> onRetry(e) }.subscribeScoped { }
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
            logger.d<AutoFishing> { "recast rod" }
            recast()
        }

    private fun recast() {
        if (rod.use()) {
            rod.rodItem.let {
                if (it == null) {
                    logger.w<AutoFishing> { "missed rod item" }
                }
            }
        } else {
            logger.d<AutoFishing> { "recast not success" }
        }
    }

    private fun onRetry(it: Throwable): Boolean {
        if (it !is TimeoutException) {
            return false
        }

        logger.w<AutoFishing> { "Recast action has taken longer than $timeout" }

        return true
    }

    companion object {
        private val timeout = 3.seconds

        private val vecComponents = arrayOf(Vec3d::x, Vec3d::y, Vec3d::z)
    }
}
