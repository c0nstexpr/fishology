package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.doOnAfterSubscribe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame

class DiscardLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    var lootsFilter: Set<FishingLoot> = setOf(),
) : SwitchDisposable() {
    private var notified = false

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        notified = false

        return caught.filter { lootsFilter.contains(it.stack.getLoot()) }
            .map { it.stack.copy() }
            .switchMapMaybe(::onCaughtExcluded)
            .tryOn()
            .subscribe {
                it.run {
                    dropSelectedItem(false)
                    swingHand(Hand.MAIN_HAND)
                }
            }
    }

    private fun onCaughtExcluded(stack: ItemStack) = SlotUpdateEvent.observable
        .mapNotNull { mapSlopUpdate(it, stack) }
        .firstOrComplete()
        .flatMap { it.onSlotUpdate(stack) }

    private fun mapSlopUpdate(it: SlotUpdateEvent.Arg, stack: ItemStack) = rod.player.let { p ->
        if (p == null) {
            logger.w("client player is null")
            null
        } else {
            if (it.stack.isSame(stack)) {
                when (it.syncId) {
                    ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID -> it.slot
                    0 -> p.playerScreenHandler?.run { getSlot(it.slot).index } ?: run {
                        logger.w("player screen handler is null")
                        null
                    }

                    else -> null
                }
            } else {
                null
            }?.let { Pair(p, it) }
        }
    }

    private fun Pair<ClientPlayerEntity, Int>.onSlotUpdate(stack: ItemStack) =
        if (first.validateSelectedSlot()) {
            rod.client.interactionManager?.run {
                SelectedSlotUpdateEvent.observable
                    .filter { stack.isSame(first.inventory?.mainHandStack) }
                    .firstOrComplete()
                    .map { first }
                    .doOnAfterSubscribe { pickFromInventory(second) }
            } ?: run {
                logger.w("interaction manager is null")
                maybeOfEmpty()
            }
        } else {
            maybeOfEmpty()
        }

    private fun ClientPlayerEntity.validateSelectedSlot() = rod.run {
        logger.d("detected excluded loots")

        if (rodItem?.slotIndex == inventory.selectedSlot) {
            logger.d("rod is selected, aborting")

            if (!notified) {
                client.msg(Text.translatable("$modId.excluded_loots_notification"))
                notified = true
            }

            false
        } else {
            true
        }
    }
}
