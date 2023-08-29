package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.submit
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityRmovEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import kotlin.time.Duration.Companion.seconds

class AutoFishing(
    private val rod: Rod,
    private val caughtItem: Observable<ItemEntity>,
) : SwitchDisposable() {
    override fun onEnable() = disposableScope {
        var player = null as PlayerEntity?
        var recast: Boolean

        fun Entity?.isLower(itemY: Double): Boolean {
            val y = this?.pos?.y ?: return false
            return y - itemY >= 0.01
        }

        caughtItem.switchMap { item ->
            val itemId = item.id
            merge(
                ItemEntityVelEvent.observable.map { it.entity }
                    .filter { it.run { id == itemId && velocity.y <= 0.0 && player.isLower(pos.y) } },
                ItemEntityRmovEvent.observable.map { it.entity }.filter { it.id == itemId },
            )
        }
            .subscribeScoped {
                logger.d("recast rod")
                rod.use()
                recast = true
            }

        CaughtFishEvent.observable.filter {
            it.run { (rod.rodItem?.player?.uuid == bobber.playerOwner?.uuid) && caught }
        }
            .subscribeScoped {
                logger.d("retrieve rod")
                rod.use()
                player = it.bobber.playerOwner
                recast = false

                ioScheduler.submit(3.seconds) {
                    if (!recast) {
                        logger.w("Recast action has taken longer than 3 seconds")
                    }
                }
            }
    }
}
