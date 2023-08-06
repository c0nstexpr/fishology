package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.FishingBobberEntity

class HookedEvent private constructor() {
    data class Arg(val bobber: FishingBobberEntity, val hook: Entity)

    companion object {
        @JvmField
        internal val subject = PublishSubject<Arg>()

        val observable: Observable<Arg> = subject
    }
}
