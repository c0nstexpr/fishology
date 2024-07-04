package org.c0nstexpr.fishology.interact

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.utils.getSlotInHand
import org.c0nstexpr.fishology.utils.isSame

class RodItem(
    val player: ClientPlayerEntity,
    val isThrow: Boolean = false,
    hand: Hand = Hand.MAIN_HAND
) {
    val slotIndex = player.inventory.getSlotInHand(hand)

    var stack: ItemStack = player.getStackInHand(hand)
        private set

    val hand
        get() = if (slotIndex == PlayerInventory.OFF_HAND_SLOT) Hand.OFF_HAND
        else Hand.MAIN_HAND

    fun updateStack() {
        stack = player.getStackInHand(hand)
    }

    fun isValid() = player.getStackInHand(hand).isSame(stack) && !stack.isEmpty
}
