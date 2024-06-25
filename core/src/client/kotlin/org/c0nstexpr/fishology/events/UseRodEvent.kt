package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.Hand

class UseRodEvent private constructor() {
    data class Arg(val hand: Hand, val player: ClientPlayerEntity, val isThrow: Boolean)

    companion object {
        @JvmField
        internal val useSubject = PublishSubject<Arg>()

        val observable: Observable<Arg> = useSubject
    }
}
