package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityRemovedEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.utils.SwitchDisposable
import java.util.*

class ThrowLoot(
    val playerUuid: UUID,
    val caughtItem: Observable<ItemEntity>,
    var lootsFilter: Set<FishingLoot> = setOf()
) : SwitchDisposable() {

    private fun onLoot(player: PlayerEntity, item: ItemEntity) {
        ItemEntityRemovedEvent.observable.map { it.entity }

        SlotUpdateEvent.observable.filter { lootsFilter.contains(it.stack.getLoot()) }
            .subscribe {
                player.dropItem(it.stack, false, true)
                player.playerScreenHandler.slots[it.slot].markDirty()
            }
    }

    override fun onEnable() = disposableScope {
        var player = null as PlayerEntity?

        CaughtFishEvent.observable.filter { it.caught }
            .mapNotNull { it.bobber.playerOwner }
            .filter { it.uuid == playerUuid }
            .subscribeScoped { player = it }

        caughtItem.mapNotNull { player?.let { p -> Pair(p, it) } }
            .subscribeScoped { onLoot(it.first, it.second) }
    }
}
