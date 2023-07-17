package org.c0nstexpr.fishology.core.events

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.minecraft.item.Item

class ItemCoolDownEvent private constructor() {
    data class Arg(val item: Item)

    companion object {
        @JvmField
        internal val mutableFlow = MutableSharedFlow<Arg>()

        val flow = mutableFlow.asSharedFlow()
    }
}
