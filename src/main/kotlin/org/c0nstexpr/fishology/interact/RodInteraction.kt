@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.observable.*
import kotlinx.coroutines.GlobalScope
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
    val beforeUseObservable = UseRodEvent.beforeUseObservable.filter { isSamePlayer(it) }

    val player get() = client.player

    private fun isSamePlayer(arg: UseRodEvent.Arg?) =
        (player != null) && (arg?.player?.uuid == player?.uuid)

    private class Item(val hand: Hand, player: PlayerEntity) {
        val slotIndex = player.getSlotInHand(hand)
        val stack: ItemStack = player.getStackInHand(hand)
    }

    private var item: Item? = null

    init {
        beforeUseObservable.subscribeScoped { item = it.let { Item(it.hand, it.player) } }
    }

    fun use(coolDownCallback: (Boolean) -> Unit = {}) {
        val player = player ?: return
        val item = item ?: return

        if (!verifyStackInHand(player, item)) {
            this.item = null
            coolDownCallback(false)
            return
        }

        client.execute {
            client.interactionManager?.interactItem(player, item.hand)

            ItemCoolDownEvent.observable
                .filter { it.item is FishingRodItem }
                .take(1).subscribeScoped {
                    coolDownCallback(true)
                }
        }
    }

    companion object {
        private fun verifyStackInHand(player: ClientPlayerEntity, item: Item) = item.run {
            (slotIndex == player.getSlotInHand(hand)) &&
                areEqual(stack, player.inventory.getStack(slotIndex))
        }
    }
}
