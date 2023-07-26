package org.c0nstexpr.fishology.core.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.FishingBobberEntity

class EntityFallingEvent private constructor() {
    data class Arg(val entity: Entity)

    companion object {
        @JvmField
        internal val subject = PublishSubject<Arg>()

        val observable: Observable<Arg> = subject
    }
}
