package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.base.exceptions.TimeoutException
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.filter
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.maybe.timeout
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.doOnBeforeSubscribe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.scheduler.ioScheduler
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.swapHand
import kotlin.time.Duration.Companion.seconds

class AutoFishing(private val rod: Rod, private val caughtItem: Observable<ItemEntity?>) : SwitchDisposable() {
    override fun onEnable() = disposableScope {
        caughtItem.filter { it == null }
            .tryOn()
            .subscribeScoped {
                logger.d<AutoFishing> { "retrieve rod" }
                rod.use()
            }

        observeCaughtItem().tryOn { _, e -> onRetry(e) }.subscribeScoped { }
    }

    private fun recastOnFailed() = HookedEvent.observable
        .filter {
            val id = rod.bobber?.id ?: return@filter false
            it.bobber.id == id
        }.mapNotNull { it.hook }
        .doOnBeforeSubscribe { recast() }
        .switchMapMaybe {
            val emptyObservable = maybeOfEmpty<Unit>()
            val player = rod.player ?: return@switchMapMaybe emptyObservable
            val manager = rod.client.interactionManager ?: return@switchMapMaybe emptyObservable
            val inv = player.inventory
            var selected = inv.selectedSlot

            if (rod.rodItem?.slotIndex == selected) {
                manager.pickFromInventory(if (selected == 0) 1 else 0)
                manager.pickFromInventory(selected)
                recast()
                return@switchMapMaybe emptyObservable
            }

            val offHandUpdate = offHandUpdate().doOnBeforeSubscribe {
                player.networkHandler.swapHand()
            }.firstOrComplete()

            offHandUpdate.map {
                selected = inv.selectedSlot
                manager.pickFromInventory(if (selected == 0) 1 else 0)
                manager.pickFromInventory(selected)
                offHandUpdate
            }
                .filter { _ -> rod.rodItem?.hand == Hand.OFF_HAND }
                .map { recast() }
        }

    private fun offHandUpdate() = SlotUpdateEvent.observable
        .filter { it.slot == PlayerInventory.OFF_HAND_SLOT }

    private fun observeCaughtItem() = caughtItem.notNull()
        .switchMapMaybe { caught ->
            fun isSameItem(it: ItemEntity) = it.id == caught.id

            merge(
                ItemEntityVelEvent.observable.map { it.entity }
                    .filter(::isSameItem)
                    .filter {
                        val playerPos = rod.player?.pos ?: return@filter false

                        vecComponents.run {
                            any { it(caught.velocity) <= 0.0 } ||
                                all { (it(playerPos) - it(caught.pos) > 0.01) }
                        }
                    },
                ItemEntityRemoveEvent.observable.map { it.entity }
                    .filter(::isSameItem)
            ).firstOrComplete().timeout(timeout, ioScheduler)
        }.switchMap { recastOnFailed() }

    private fun recast() {
        logger.d<AutoFishing> { "recast rod" }
        if (rod.use()) rod.rodItem.let {
            if (it == null) logger.w<AutoFishing> { "missed rod item" }
        }
        else logger.d<AutoFishing> { "recast not success" }
    }

    private fun onRetry(it: Throwable): Boolean {
        if (it !is TimeoutException) return false

        logger.w<AutoFishing> { "Recast action has taken longer than $timeout" }

        return true
    }

    companion object {
        private val timeout = 3.seconds

        private val vecComponents = arrayOf(Vec3d::x, Vec3d::y, Vec3d::z)
    }
}
