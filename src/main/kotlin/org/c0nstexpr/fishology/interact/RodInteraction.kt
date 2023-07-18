@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.FishingRodItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemStack.areEqual
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.ItemCoolDownEvent
import org.c0nstexpr.fishology.core.events.UseRodEvent
import org.c0nstexpr.fishology.utils.coroutineScope
import org.c0nstexpr.fishology.utils.getSlotInHand

class RodInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val beforeUseFlow = UseRodEvent.beforeUseFlow.filter { isSamePlayer(it) }

    val afterUseFlow = UseRodEvent.afterUseFlow.filter { isSamePlayer(it) }

    val player get() = client.player

    private fun isSamePlayer(arg: UseRodEvent.Arg?) =
        (player != null) && (arg?.player?.uuid == player?.uuid)

    class Item(val hand: Hand, player: PlayerEntity) {
        val slotIndex = player.getSlotInHand(hand)
        val stack: ItemStack = player.getStackInHand(hand)
    }

    var item: Item? = null
        private set

    init {
        val job = client.coroutineScope.launch {
            beforeUseFlow.collect { item = it?.let { Item(it.hand, it.player) } }
        }
        doOnDispose(job::cancel)
    }

    suspend fun use() {
        val player = player ?: return
        val item = item ?: return

        val interact = {
            if (verifyStackInHand(player, item)) {
                client.interactionManager?.interactItem(player, item.hand)
            } else {
                this.item = null
            }
        }

        if (!player.itemCooldownManager.isCoolingDown(player.getStackInHand(item.hand)?.item)) {
            interact()
            return
        }

        ItemCoolDownEvent.flow
            .filter { it.item is FishingRodItem }
            .take(1)
            .collect { client.coroutineScope.launch { interact() }.join() }
    }

    companion object {
        private fun verifyStackInHand(player: ClientPlayerEntity, item: Item) = item.run {
            (slotIndex == player.getSlotInHand(hand)) &&
                areEqual(stack, player.inventory.getStack(slotIndex))
        }
    }
}
