package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.utils.SwitchDisposable

class ThrowLoot(val player: ClientPlayerEntity) : SwitchDisposable() {
    var lootsFilter = setOf<FishingLoot>()

    private fun onLoot(slot: Int) {
        player.dropItem(player.inventory.getStack(slot), false, true)
    }

    override fun onEnable() =
        SlotUpdateEvent.observable.filter { lootsFilter.contains(it.stack.getLoot()) }
            .subscribe { onLoot(it.slot) }
}
