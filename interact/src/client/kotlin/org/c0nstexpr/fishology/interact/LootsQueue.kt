package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.subscribe
import net.minecraft.text.Text
import org.c0nstexpr.fishology.MOD_ID
import org.c0nstexpr.fishology.msg

internal class LootsQueue(val rod: Rod) : DisposableScope by DisposableScope() {
    private val queue = ArrayList<FishingLootSlot>(3)

    private var notified = false

    private var current = Disposable().apply { dispose() }

    private val lock = Any()

    private val player get() = rod.player

    init {
        doOnDispose {
            synchronized(lock) {
                queue.clear()
                current.dispose()
            }
        }
    }

    fun add(loot: FishingLootSlot) = synchronized<Unit>(lock) {
        if (current.isDisposed) {
            current = subscribeTo(loot.dropMaybe(player))

            if (!loot.pick()) current.dispose()
        } else {
            queue.add(loot)
        }
    }

    private fun subscribeTo(maybe: Maybe<Unit>): Disposable = maybe.subscribe {
        synchronized(lock) {
            current.dispose()

            if (queue.isEmpty()) return@subscribe

            val loot = queue.removeFirst()
            current = subscribeTo(loot.dropMaybe(player))
            if (!loot.pick()) current.dispose()
        }
    }

    private fun FishingLootSlot.pick() = pick(player, rod.client.interactionManager, rod.rodItem) {
        if (!notified) {
            rod.client.msg(Text.translatable("$MOD_ID.discard_loots_notification"))
            notified = true
        }
    }
}
