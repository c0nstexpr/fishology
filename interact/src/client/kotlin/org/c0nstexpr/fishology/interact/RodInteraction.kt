package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemStack.areEqual
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.events.UseRodEvent
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
        val item = item

        logger.d("using rod")

        if (!verifyStackInHand(player, item)) {
            logger.d("rod status not match, aborting")
            return callback(false)
        }

        client.execute {
            logger.d("use rod")
            client.interactItem(item!!.hand)
            callback(true)
        }
    }

    companion object {
        private fun verifyStackInHand(player: PlayerEntity?, item: Item?): Boolean {
            if (player == null) {
                logger.d("no player instance, aborting")
                return false
            }

            if (item == null) {
                logger.d("no rod item, aborting")

                return false
            }

            return item.run {
                (slotIndex == player.getSlotInHand(hand)) &&
                        areEqual(stack, player.inventory.getStack(slotIndex))
            }
        }
    }
}
