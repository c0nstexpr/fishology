package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.coroutinesinterop.asScheduler
import com.badoo.reaktive.observable.filter
import com.github.shynixn.mccoroutine.fabric.minecraftDispatcher
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
import org.c0nstexpr.fishology.FishologyMod
import org.c0nstexpr.fishology.events.SetFishHookEvent

val vecComponents = arrayOf(Vec3d::x, Vec3d::y, Vec3d::z)

fun PlayerInventory.getSlotInHand(hand: Hand) = when (hand) {
    Hand.MAIN_HAND -> selectedSlot
    Hand.OFF_HAND -> PlayerInventory.OFF_HAND_SLOT
}

fun MinecraftClient.interactItem(hand: Hand) = this.interactionManager?.interactItem(player, hand)

fun ItemStack.isSame(other: ItemStack?) =
    if (other == null) false else ItemStack.areItemsAndComponentsEqual(this, other)

val Entity.trackedPos: Vec3d get() = this.trackedPosition.pos

fun ClientPlayerEntity.swapHand() = networkHandler.sendPacket(
    PlayerActionC2SPacket(
        PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
        BlockPos.ORIGIN,
        Direction.DOWN
    )
)

fun fishHookRemovedObservable() = SetFishHookEvent.observable.filter { it.bobber == null }

fun clientScheduler() = FishologyMod.minecraftDispatcher.asScheduler()
