package org.c0nstexpr.fishology.events

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject

class InventoryUpdateEvent private constructor() {
    companion object {
        @JvmField
        internal val subject = PublishSubject<Unit>()

        val observable: Observable<Unit> = subject
    }
}
