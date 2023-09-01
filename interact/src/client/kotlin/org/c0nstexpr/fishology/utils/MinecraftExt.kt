package org.c0nstexpr.fishology.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d

fun PlayerInventory.getSlotInHand(hand: Hand) = when (hand) {
    Hand.MAIN_HAND -> selectedSlot
    Hand.OFF_HAND -> PlayerInventory.OFF_HAND_SLOT
}

fun MinecraftClient.interactItem(hand: Hand) =
    this.interactionManager?.interactItem(player, hand)

fun ItemStack.isSame(other: ItemStack?): Boolean {
    return ItemStack.areEqual(this, other ?: return false)
}

val Entity.trackedPos: Vec3d get() = this.trackedPosition.withDelta(0, 0, 0)
