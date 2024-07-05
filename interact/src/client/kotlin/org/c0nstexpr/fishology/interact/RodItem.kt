package org.c0nstexpr.fishology.interact

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.utils.getSlotInHand

class RodItem(val player: ClientPlayerEntity, val isThrow: Boolean, hand: Hand) {
    val slotIndex = player.inventory.getSlotInHand(hand)

    val hand
        get() = if (slotIndex == PlayerInventory.OFF_HAND_SLOT) Hand.OFF_HAND
        else Hand.MAIN_HAND

    fun isValid() = player.getStackInHand(hand).isOf(Items.FISHING_ROD) &&
        player.inventory.getSlotInHand(hand) == slotIndex
}
