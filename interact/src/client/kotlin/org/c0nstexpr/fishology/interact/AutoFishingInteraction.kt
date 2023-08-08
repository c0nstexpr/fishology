package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import net.minecraft.entity.ItemEntity
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityFallingEvent
import org.c0nstexpr.fishology.events.ItemEntityRemovedEvent
import org.c0nstexpr.fishology.logger
import java.util.UUID

class AutoFishingInteraction(
    val useRod: (callback: (Boolean) -> Unit) -> Unit,
    var playerUuid: UUID,
    var caughtItem: Observable<ItemEntity>,
) : DisposableScope by DisposableScope() {
    init {
        CaughtFishEvent.observable.filter {
            it.run { (playerUuid == bobber.playerOwner?.uuid) && caught }
        }.subscribeScoped {
            logger.d("retrieve rod")
            useRod {}
        }

        val caughtDisposable = SerialDisposable()

        caughtItem.subscribeScoped { item ->
            caughtDisposable.set(
                merge(
                    ItemEntityFallingEvent.observable.map { it.entity },
                    ItemEntityRemovedEvent.observable.map { it.entity },
                ).filter { it.uuid == item.uuid }
                    .subscribeScoped {
                        logger.d("recast rod")
                        useRod {}
                    },
            )
        }

        caughtDisposable.scope()
    }
}
