package org.c0nstexpr.fishology.utils

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.scheduler.Scheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.ItemEntitySpawnEvent
import org.c0nstexpr.fishology.events.ItemEntityTrackerEvent
import org.c0nstexpr.fishology.events.SetFishHookEvent
import kotlin.time.Duration

val vecComponents = arrayOf(Vec3d::x, Vec3d::y, Vec3d::z)

fun PlayerInventory.getSlotInHand(hand: Hand) = when (hand) {
    Hand.MAIN_HAND -> selectedSlot
    Hand.OFF_HAND -> PlayerInventory.OFF_HAND_SLOT
}

fun MinecraftClient.interactItem(hand: Hand) = this.interactionManager?.interactItem(player, hand)

fun ItemStack.isSame(other: ItemStack?) =
    if (other == null) false else ItemStack.areItemsAndComponentsEqual(this, other)

val Entity.trackedPos: Vec3d get() = this.trackedPosition.withDelta(0, 0, 0)

fun ClientPlayerEntity.swapHand() = networkHandler.sendPacket(
    PlayerActionC2SPacket(
        PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
        BlockPos.ORIGIN,
        Direction.DOWN
    )
)

fun spawnedItemMaybe(p: (ItemEntity) -> Boolean) = ItemEntitySpawnEvent.observable
    .switchMap { ItemEntityTrackerEvent.observable.map { it.entity }.filter { p(it) } }
    .firstOrComplete()

fun fishHookRemovedObservable() = SetFishHookEvent.observable.filter { it.bobber == null }

class MCClientScheduler(val client: MinecraftClient = MinecraftClient.getInstance()) :
    Scheduler,
    DisposableScope by DisposableScope() {
    override fun destroy() {
        dispose()
    }

    override fun newExecutor() = object : Scheduler.Executor {
        override var isDisposed = this@MCClientScheduler.isDisposed
            private set

        override fun cancel() {
            isDisposed = true
        }

        override fun dispose() {
            isDisposed = true
        }

        override fun submit(delay: Duration, period: Duration, task: () -> Unit) {
            if (isDisposed) return

            client.submit {
                runBlocking {
                    delay(delay)

                    if (period.isInfinite()) {
                        task()
                        return@runBlocking
                    }

                    while (!isDisposed) {
                        task()
                        delay(period)
                    }
                }
            }
        }
    }.scope()
}
