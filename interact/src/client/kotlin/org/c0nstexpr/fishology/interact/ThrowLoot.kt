package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import net.minecraft.entity.ItemEntity
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.ItemEntityRemovedEvent
import org.c0nstexpr.fishology.utils.SwitchDisposable

class ThrowLoot(
    private val caught: Observable<ItemEntity>,
) : SwitchDisposable() {
    var lootsFilter = setOf<FishingLoot>()

    private fun onCaught(stack: ItemEntity) {
        ItemEntityRemovedEvent.observable.map { it.entity }
    }

    override fun onEnable() =
        caught.filter { lootsFilter.contains(it.stack.getLoot()) }
            .subscribe { onCaught(it) }
}
