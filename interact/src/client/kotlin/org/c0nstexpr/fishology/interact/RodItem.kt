package org.c0nstexpr.fishology.interact

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.utils.getSlotInHand
import org.c0nstexpr.fishology.utils.isSame

class RodItem(
    hand: Hand = Hand.MAIN_HAND,
    player: PlayerEntity? = null,
    val isThrow: Boolean = false
) {
    val slotIndex = player?.inventory?.getSlotInHand(hand) ?: -1
    val stack: ItemStack = player?.getStackInHand(hand) ?: ItemStack.EMPTY

    val hand
        get() = if (slotIndex == PlayerInventory.OFF_HAND_SLOT) Hand.OFF_HAND
        else Hand.MAIN_HAND

    fun isValid(player: PlayerEntity?) =
        player?.inventory?.getStack(slotIndex)?.isSame(stack) ?: false
}
