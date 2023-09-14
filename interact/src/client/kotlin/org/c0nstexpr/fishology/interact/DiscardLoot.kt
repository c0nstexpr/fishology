package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame
import org.c0nstexpr.fishology.utils.observableStep

class DiscardLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    lootsFilter: Set<FishingLoot> = setOf(),
) : SwitchDisposable() {
    var lootsFilter = lootsFilter
        set(value) {
            field = value
            logger.d("Change discard loots")
        }

    private val lootsQueue = LootsQueue(rod)

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        return disposableScope {
            observableStep(
                caught.filter { lootsFilter.contains(it.stack.getLoot()) }.map { it.stack.copy() },
            )
                .switchMaybe(
                    {
                        SlotUpdateEvent.observable.mapNotNull { mapSlopUpdate(it) }
                            .firstOrComplete()
                    },
                ) {
                    logger.d("detected excluded loots")
                    lootsQueue.add(this)
                }
                .tryOn()
                .subscribeScoped { }
        }
    }

    private fun ItemStack.mapSlopUpdate(arg: SlotUpdateEvent.Arg): FishingLootSlot? {
        val player = rod.player ?: run {
            logger.w("client player is null")
            return null
        }

        return if (arg.stack.isSame(this) && arg.syncId == player.playerScreenHandler.syncId) {
            FishingLootSlot(
                player.playerScreenHandler.getSlot(arg.slot).index,
                this,
            )
        } else {
            logger.w("client player is null")
            null
        }
    }
}
