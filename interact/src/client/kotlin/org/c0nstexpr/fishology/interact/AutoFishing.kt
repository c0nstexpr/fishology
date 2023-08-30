package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.base.exceptions.TimeoutException
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.doOnAfterSubscribe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.timeout
import com.badoo.reaktive.scheduler.ioScheduler
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityRmovEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import kotlin.time.Duration.Companion.seconds

class AutoFishing(
    private val rod: Rod,
    private val caughtItem: Observable<ItemEntity?>,
) : SwitchDisposable() {
    private val player get() = rod.player

    override fun onEnable() = CaughtFishEvent.observable.filter { it.caught }
        .map { it.bobber }
        .filter { player?.uuid == it.playerOwner?.uuid }
        .switchMap { onCaughtFish() }
        .subscribe(onError = ::onError) {
            logger.d("recast rod")
            rod.use()
        }

    private fun onError(it: Throwable) {
        if (it !is TimeoutException) {
            logger.e(it.localizedMessage)
            return
        }

        logger.w("Recast action has taken longer than $timeout")
    }

    private fun onCaughtFish() = caughtItem.switchMap(::onCaughtItem)
        .doOnAfterSubscribe {
            logger.d("retrieve rod")
            rod.use()
        }

    private fun onCaughtItem(item: ItemEntity?): Observable<ItemEntity> {
        fun isSameItem(it: ItemEntity) = it.id == item.id

        fun Entity?.isHigher(itemY: Double): Boolean {
            return (this?.pos?.y ?: return false) - itemY >= 0.01
        }

        return merge(
            ItemEntityVelEvent.observable.map { it.entity }
                .filter(::isSameItem)
                .filter { it.run { velocity.y <= 0.0 && player.isHigher(pos.y) } },
            ItemEntityRmovEvent.observable.map { it.entity }.filter(::isSameItem),
        )
            .timeout(timeout, ioScheduler)
    }

    companion object {
        private val timeout = 3.seconds
    }
}
