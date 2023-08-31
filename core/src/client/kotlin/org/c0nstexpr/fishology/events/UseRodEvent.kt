package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand

class UseRodEvent private constructor() {
    data class Arg(val hand: Hand, val player: PlayerEntity, val isThrow: Boolean)

    companion object {
        @JvmField
        internal val useSubject = PublishSubject<Arg>()

        val observable: Observable<Arg> = useSubject
    }
}
