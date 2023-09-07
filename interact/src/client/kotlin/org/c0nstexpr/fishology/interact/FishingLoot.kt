package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.doOnBeforeSubscribe
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.merge
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.zip
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.isSame

internal class FishingLoot(val player: ClientPlayerEntity, val slot: Int, val stack: ItemStack) {
    fun pick(manager: ClientPlayerInteractionManager?, rodItem: RodItem?, notify: () -> Unit) =
        if (manager == null) {
            logger.w("interaction manager is null")
            false
        } else if (rodItem?.slotIndex == player.inventory.selectedSlot) {
            logger.d("rod is selected, aborting")
            notify()
            false
        } else if (player.inventory.getStack(slot).isSame(stack)) {
            manager.pickFromInventory(slot)
            true
        } else {
            logger.w("loot slot is not same as loot")
            false
        }

    fun drop() = player.run {
        if (!inventory.mainHandStack.isSame(stack)) {
            logger.w("selected stack is not same as loot")
        } else if (dropSelectedItem(false)) {
            logger.d("dropped excluded loot")
            swingHand(Hand.MAIN_HAND)
        } else {
            logger.w("failed to drop discard loot")
        }
    }

    fun dropMaybe(cancelObservable: Observable<Unit?>): Maybe<FishingLoot?> {
        val inventory = player.inventory

        return merge(
            zip(
                slotUpdateObservable.filter { it.slot == slot }
                    .map { logger.d("screen loot's slot update") },
                slotUpdateObservable.filter { it.slot == inventory.selectedSlot }
                    .map { logger.d("screen selected slot update") },
                SelectedSlotUpdateEvent.observable.filter { stack.isSame(inventory.mainHandStack) }
                    .map { logger.d("selected slot update") }
            ) { _, _, _ -> }.map { this },
            cancelObservable.map { null }
        ).firstOrComplete()
    }

    companion object {
        private val slotUpdateObservable =
            SlotUpdateEvent.observable.filter { it.syncId == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID }

    }
}