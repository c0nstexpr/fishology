package org.c0nstexpr.fishology.core.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.FishingRodItem
import net.minecraft.util.Hand

class UseRodEvent private constructor() {
    data class Arg(
        val hand: Hand,
        val player: PlayerEntity
    )

    companion object {
        @JvmField
        internal val afterSubject = PublishSubject<Arg>()

        val afterUse: Observable<Arg> = afterSubject

        @JvmField
        internal val beforeSubject = PublishSubject<Arg>()

        val beforeUse: Observable<Arg> = beforeSubject
    }
}
