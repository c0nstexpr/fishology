package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import net.minecraft.entity.ItemEntity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityFallingEvent
import org.c0nstexpr.fishology.events.ItemEntityRemovedEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import java.util.*

class AutoFishing(
    private val useRod: () -> Unit,
    var playerUuid: UUID,
    private val caughtItem: Observable<ItemEntity>,
) : SwitchDisposable() {
    override fun onEnable() = disposableScope {
        val caughtDisposable = SerialDisposable()

        caughtItem.subscribeScoped { item ->
            caughtDisposable.set(
                merge(
                    ItemEntityFallingEvent.observable.map { it.entity },
                    ItemEntityRemovedEvent.observable.map { it.entity },
                ).filter { it.uuid == item.uuid }
                    .firstOrComplete()
                    .subscribe {
                        logger.d("recast rod")
                        useRod()
                        caughtDisposable.set(null)
                    },
            )
        }

        CaughtFishEvent.observable.filter {
            it.run { (playerUuid == bobber.playerOwner?.uuid) && caught }
        }.subscribeScoped {
            logger.d("retrieve rod")
            useRod()
        }

        caughtDisposable.scope()
    }
}
