package org.c0nstexpr.fishology.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.Hand

fun PlayerEntity.getSlotInHand(hand: Hand) = when (hand) {
    Hand.MAIN_HAND -> inventory.selectedSlot
    Hand.OFF_HAND -> PlayerInventory.OFF_HAND_SLOT
}

fun MinecraftClient.interactItem(hand: Hand) =
        this.interactionManager?.interactItem(player, hand)
