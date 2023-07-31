package org.c0nstexpr.fishology.core.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand

class UseRodEvent private constructor() {
    data class Arg(
            val hand: Hand,
            val player: PlayerEntity,
    )

    companion object {
        @JvmField
        internal val beforeUseSubject = PublishSubject<Arg>()

        val beforeUseObservable: Observable<Arg> = beforeUseSubject

        @JvmField
        internal val afterUseSubject = PublishSubject<Arg>()

        val afterUseObservable: Observable<Arg> = beforeUseSubject
    }
}
