package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient

class ClientTickEvent private constructor() {
    data class Arg(val client: MinecraftClient)

    companion object {
        @JvmField
        internal val subject =
            PublishSubject<Arg>()
                .apply { ClientTickEvents.END_CLIENT_TICK.register { onNext(Arg(it)) } }

        val observable: Observable<Arg> = subject
    }
}
