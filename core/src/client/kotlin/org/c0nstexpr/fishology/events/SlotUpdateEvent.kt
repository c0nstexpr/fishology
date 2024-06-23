package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.item.ItemStack

class SlotUpdateEvent private constructor() {
    data class Arg(val slot: Int, val stack: ItemStack, val syncId: Int)

    companion object {
        @JvmField
        internal val subject = PublishSubject<Arg>()

        val observable: Observable<Arg> = subject
    }
}
