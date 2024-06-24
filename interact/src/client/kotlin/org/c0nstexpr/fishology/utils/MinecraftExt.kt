package org.c0nstexpr.fishology.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

fun PlayerInventory.getSlotInHand(hand: Hand) = when (hand) {
    Hand.MAIN_HAND -> selectedSlot
    Hand.OFF_HAND -> PlayerInventory.OFF_HAND_SLOT
}

fun MinecraftClient.interactItem(hand: Hand) = this.interactionManager?.interactItem(player, hand)

fun ItemStack.isSame(other: ItemStack?) =
    if (other == null) false else ItemStack.areItemsAndComponentsEqual(this, other)

val Entity.trackedPos: Vec3d get() = this.trackedPosition.withDelta(0, 0, 0)

fun ClientPlayerEntity.swapHand() {
    val offHandStack = offHandStack

    setStackInHand(Hand.OFF_HAND, mainHandStack)
    setStackInHand(Hand.MAIN_HAND, offHandStack)

    networkHandler.sendPacket(
        PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
            BlockPos.ORIGIN,
            Direction.DOWN
        )
    )
}
