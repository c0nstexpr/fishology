package org.c0nstexpr.fishology.core.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.item.Item

class ItemCooldownEvent private constructor() {
    data class Arg(val item: Item)

    companion object {
        @JvmField
        internal val subject = PublishSubject<Arg>()

        val observable: Observable<Arg> = subject
    }
}
