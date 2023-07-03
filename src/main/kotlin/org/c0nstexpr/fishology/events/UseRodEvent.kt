package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.item.FishingRodItem
import net.minecraft.util.Hand

class UseRodEvent private constructor() {
    data class Arg(val item: FishingRodItem, val hand: Hand)

    companion object {
        @JvmField
        internal val subject = PublishSubject<Arg>()

        val observable: Observable<Arg> = subject
    }
}
