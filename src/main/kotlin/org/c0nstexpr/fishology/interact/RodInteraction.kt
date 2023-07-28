package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemStack.areEqual
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.UseRodEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.getSlotInHand
import org.c0nstexpr.fishology.utils.interactItem

class RodInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val beforeUseObservable = UseRodEvent.beforeUseObservable.filter { isSamePlayer(it.player) }
    val afterUseObservable = UseRodEvent.afterUseObservable.filter { isSamePlayer(it.player) }

    val player get() = client.player

    private fun isSamePlayer(p: Entity) = (player != null) && (p.uuid == player?.uuid)

    private class Item(val hand: Hand, player: PlayerEntity) {
        val slotIndex = player.getSlotInHand(hand)
        val stack: ItemStack = player.getStackInHand(hand)
    }

    private var item: Item? = null

    init {
        logger.d("Initializing rod interaction")
        beforeUseObservable.subscribeScoped {
            logger.d("detected rod use, saving rod status")
            item = it.let { Item(it.hand, it.player) }
        }
    }

    fun use(callback: (Boolean) -> Unit) {
        val player = player ?: return callback(false)
        val item = item ?: return callback(false)

        logger.d("using rod")

        if (!verifyStackInHand(player, item)) {
            this.item = null
            logger.d("invalid rod status, aborting use command")
            return callback(false)
        }

        client.execute {
            client.interactItem(item.hand)
            callback(true)
        }
    }

    companion object {
        private fun verifyStackInHand(player: ClientPlayerEntity, item: Item) = item.run {
            (slotIndex == player.getSlotInHand(hand)) &&
                    areEqual(stack, player.inventory.getStack(slotIndex))
        }
    }
}
