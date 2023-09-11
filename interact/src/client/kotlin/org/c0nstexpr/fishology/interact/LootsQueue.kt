package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.subscribe

internal class LootsQueue : DisposableScope by DisposableScope() {
    private val queue = ArrayList<FishingLootSlot>(3)

    private var current = Disposable().apply { dispose() }

    private val lock = Any()

    init {
        doOnDispose {
            synchronized(lock) {
                queue.clear()
                current.dispose()
            }
        }
    }

    fun add(loot: FishingLootSlot) = synchronized(lock) {
        if (current.isDisposed) {
            current = subscribeTo(loot.dropMaybe())
        } else {
            queue.add(loot)
        }
    }

    private fun subscribeTo(maybe: Maybe<Unit>): Disposable = maybe.subscribe {
        synchronized(lock) {
            current.dispose()
            current = subscribeTo(queue.removeFirst().dropMaybe())
        }
    }
}
