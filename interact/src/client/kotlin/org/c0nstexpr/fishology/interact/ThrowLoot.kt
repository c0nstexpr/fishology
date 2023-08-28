package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable

class ThrowLoot(
    val caughtItem: Observable<Pair<ClientPlayerEntity, ItemEntity>>,
    var lootsFilter: Set<FishingLoot> = setOf(),
) : SwitchDisposable() {
    override fun onEnable() = disposableScope {
        var player = null as ClientPlayerEntity?
        var item = null as ItemEntity?

        caughtItem.filter { lootsFilter.contains(it.second.stack.getLoot()) }
            .subscribeScoped {
                player = it.first
                item = it.second
            }

        SlotUpdateEvent.observable.filter {
            val stack = item?.stack
            if (stack == null) {
                false
            } else {
                ItemStack.areEqual(it.stack, stack)
            }
        }
            .subscribeScoped {
                player?.run {
                    logger.d("detected excluded loots")

                    dropItem(it.stack, false, true)
                    playerScreenHandler.slots[it.slot].markDirty()
                }
            }
    }
}
