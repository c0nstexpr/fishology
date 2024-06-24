package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.concatMapMaybe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import net.minecraft.entity.ItemEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.MOD_ID
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame

class DiscardLoot(private val rod: Rod, private val caught: Observable<ItemEntity>) :
    SwitchDisposable() {
    var lootsFilter = setOf<FishingLoot>()
        set(value) {
            field = value
            logger.d<DiscardLoot> { "Change discard loots" }
        }

    override fun onEnable(): Disposable {
        logger.d<DiscardLoot> { "enable throw loot interaction" }

        var notified = false

        val notify = {
            if (!notified) {
                rod.client.msg(Text.translatable("$MOD_ID.discard_loots_notification"))
                notified = true
            }
        }

        return caught.map { it.stack }
            .switchMapMaybe { stack ->
                val player = rod.player

                if (player == null) {
                    logger.w<DiscardLoot> { "client player is null" }
                    return@switchMapMaybe maybeOfEmpty()
                }

                if (rod.rodItem.slotIndex == player.inventory.selectedSlot) {
                    logger.d<DiscardLoot> { "rod is selected, aborting" }
                    notify()
                    return@switchMapMaybe maybeOfEmpty()
                }

                if (!lootsFilter.contains(stack.getLoot())) return@switchMapMaybe maybeOfEmpty()

                val copied = stack.copy()

                SlotUpdateEvent.observable.filter {
                    it.syncId == player.playerScreenHandler.syncId && it.stack.isSame(copied)
                }
                    .map { Pair(player.playerScreenHandler.getSlot(it.slot).index, copied) }
                    .firstOrComplete()
            }
            .concatMapMaybe { (slot, stack) ->
                val manager = rod.client.interactionManager
                val player = rod.player

                if (player == null) {
                    logger.w<DiscardLoot> { "client player is null" }
                    return@concatMapMaybe maybeOfEmpty()
                }

                if (manager == null) {
                    logger.w<DiscardLoot> { "interaction manager is null" }
                    return@concatMapMaybe maybeOfEmpty()
                }

                manager.pickFromInventory(slot)

                SelectedSlotUpdateEvent.observable.filter {
                    stack.isSame(
                        player.inventory.getStack(
                            it.slot
                        )
                    )
                }
                    .map { player }
                    .firstOrComplete()
            }
            .subscribe {
                if (it.dropSelectedItem(false)) {
                    logger.d<DiscardLoot> { "dropped excluded loot" }
                    it.swingHand(Hand.MAIN_HAND)
                } else logger.w<DiscardLoot> { "failed to drop discard loot" }
            }
    }
}
