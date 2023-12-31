package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.zip
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.isSame

internal class FishingLootSlot(val slot: Int, val stack: ItemStack) {
    fun pick(
        player: ClientPlayerEntity?,
        manager: ClientPlayerInteractionManager?,
        rodItem: RodItem?,
        notify: () -> Unit,
    ) =
        if (player == null) {
            logger.w<FishingLootSlot> { "client player is null" }
            false
        } else if (manager == null) {
            logger.w<FishingLootSlot> { "interaction manager is null" }
            false
        } else if (rodItem?.slotIndex == player.inventory.selectedSlot) {
            logger.d<FishingLootSlot> { "rod is selected, aborting" }
            notify()
            false
        } else if (player.inventory.getStack(slot).isSame(stack)) {
            manager.pickFromInventory(slot)
            true
        } else {
            logger.w<FishingLootSlot> { "loot slot is not same as loot" }
            false
        }

    fun drop(player: ClientPlayerEntity) = player.run {
        if (!inventory.mainHandStack.isSame(stack)) {
            logger.w<FishingLootSlot> { "selected stack is not same as loot" }
        } else if (dropSelectedItem(false)) {
            logger.d<FishingLootSlot> { "dropped excluded loot" }
            swingHand(Hand.MAIN_HAND)
        } else {
            logger.w<FishingLootSlot> { "failed to drop discard loot" }
        }
    }

    fun dropMaybe(player: ClientPlayerEntity?): Maybe<Unit> {
        if (player == null) {
            logger.w<FishingLootSlot> { "client player is null" }
            return maybeOfEmpty()
        }

        val inventory = player.inventory

        return zip(
            slotUpdateObservable.filter { it.slot == slot }
                .map { logger.d<FishingLootSlot> { "screen loot's slot update" } },
            slotUpdateObservable.filter { it.slot == inventory.selectedSlot }
                .map { logger.d<FishingLootSlot> { "screen selected slot update" } },
            SelectedSlotUpdateEvent.observable.filter { stack.isSame(inventory.mainHandStack) }
                .map { logger.d<FishingLootSlot> { "selected slot update" } },
        ) { _, _, _ -> }
            .firstOrComplete()
            .map { drop(player) }
    }

    companion object {
        private val slotUpdateObservable =
            SlotUpdateEvent.observable.filter { it.syncId == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID }
    }
}
