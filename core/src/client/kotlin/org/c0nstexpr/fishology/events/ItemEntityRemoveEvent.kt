package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import net.minecraft.entity.ItemEntity

class ItemEntityRemoveEvent private constructor() {
    data class Arg(val entity: ItemEntity)

    companion object {
        val observable =
            EntityRemoveEvent.observable.mapNotNull { it.entity as? ItemEntity }.map { Arg(it) }
    }
}
