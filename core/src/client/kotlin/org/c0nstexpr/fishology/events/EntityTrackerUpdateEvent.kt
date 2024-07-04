package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.data.DataTracker

class EntityTrackerUpdateEvent private constructor() {
    data class Arg(val id: Int, val entries: List<DataTracker.SerializedEntry<*>>)

    companion object {
        @JvmField
        internal val subject = PublishSubject<Arg>()

        val observable: Observable<Arg> = subject
    }
}
