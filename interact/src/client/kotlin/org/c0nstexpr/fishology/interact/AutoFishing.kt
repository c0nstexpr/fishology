package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.filter
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapMaybe
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.EntityRemoveEvent
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.swapHand
import kotlin.math.abs

class AutoFishing(private val rod: Rod, private val caughtItem: Observable<ItemEntity>) :
    SwitchDisposable() {
    override fun onEnable() = CaughtFishEvent.observable.filter { it.caught }
        .switchMapMaybe {
            logger.d<AutoFishing> { "retrieve rod" }
            rod.use()

            caughtItem.firstOrComplete()
        }
        .switchMapMaybe { caught ->
            val player = rod.player

            if (player == null) {
                logger.w<AutoFishing> { "player is null" }
                return@switchMapMaybe maybeOfEmpty<Unit>()
            }

            fun isSameItem(it: ItemEntity) = it.id == caught.id

            // observe the caught entity dropping or removed
            merge(
                ItemEntityVelEvent.observable.map { it.entity }
                    .filter {
                        isSameItem(it) &&
                            (
                                vecComponents.any { abs(it(caught.velocity)) <= 0.1 } ||
                                    caught.pos.y < player.pos.y + 1
                            )
                    },
                ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem)
            ).firstOrComplete()
        }
        .switchMap {
            recast()
            HookedEvent.observable.filter { it.hook != null }
        }
        .switchMapMaybe { hooked ->
            val player = rod.player ?: return@switchMapMaybe maybeOfEmpty()
            val inv = player.inventory
            val bobberId = hooked.bobber.id

            if (rod.rodItem.slotIndex == inv.selectedSlot) {
                return@switchMapMaybe scrollHotBar(inv, bobberId)
            }

            player.swapHand()

            return@switchMapMaybe scrollHotBar(inv, bobberId).map { player.swapHand() }
        }
        .subscribe { recast() }

    private fun scrollHotBar(inv: PlayerInventory, bobberId: Int): Maybe<Unit> {
        val swappable = inv.run {
            for (i in 0..8) {
                val j = (selectedSlot + i) % 9
                val stack = inv.main[j]
                if (!stack.isOf(Items.FISHING_ROD)) return@run j
            }

            -1
        }

        if (swappable == -1) {
            logger.d<AutoFishing> { "no swappable slot found" }
            return maybeOfEmpty()
        }

        val selected = inv.selectedSlot

        inv.selectedSlot = swappable

        return EntityRemoveEvent.observable.filter { bobberId == it.entity.id }
            .firstOrComplete()
            .map { inv.selectedSlot = selected }
    }

    private fun recast() {
        logger.d<AutoFishing> { "recast rod" }
        if (!rod.use()) logger.d<AutoFishing> { "recast not success" }
    }

    companion object {
        private val vecComponents = arrayOf(Vec3d::x, Vec3d::y, Vec3d::z)
    }
}
