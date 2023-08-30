package org.c0nstexpr.fishology.interact

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.utils.getSlotInHand
import org.c0nstexpr.fishology.utils.isSame

data class RodItem(val hand: Hand, val player: PlayerEntity) {
    val slotIndex = player.inventory.getSlotInHand(hand)
    val stack: ItemStack = player.getStackInHand(hand)

    override fun equals(other: Any?) = (other is RodItem) &&
        hand == other.hand &&
        player.uuid == other.player.uuid &&
        slotIndex == other.slotIndex &&
        stack.isSame(other.stack)

    override fun hashCode(): Int {
        var result = hand.hashCode()
        result = 31 * result + player.hashCode()
        result = 31 * result + slotIndex
        result = 31 * result + stack.hashCode()
        return result
    }
}
