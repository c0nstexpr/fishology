package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.firstOrDefault
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.SingleObserver
import com.badoo.reaktive.single.singleOf
import com.badoo.reaktive.subject.Subject
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemStack.areEqual
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.UseRodEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.getSlotInHand
import org.c0nstexpr.fishology.utils.onNextComplete

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
        logger.d("Initializing rod interaction")
        beforeUseObservable.subscribeScoped {
            logger.d("detected rod use, saving rod status")
            item = it.let { Item(it.hand, it.player) }
        }
    }

    fun use(): Single<Boolean> {
        val falseValue = singleOf(false)
        val player = player ?: return falseValue
        val item = item ?: return falseValue

        logger.d("using rod")

        if (!verifyStackInHand(player, item)) {
            this.item = null
            logger.d("invalid rod status, aborting use command")
            return falseValue
        }

        val subject = BehaviorSubject(false)
        client.execute {
            client.interactionManager?.interactItem(player, item.hand)
            subject.onNextComplete(true)
        }
        return subject.filter { it }.firstOrDefault(false)
    }

    companion object {
        private fun verifyStackInHand(player: ClientPlayerEntity, item: Item) = item.run {
            (slotIndex == player.getSlotInHand(hand)) &&
                areEqual(stack, player.inventory.getStack(slotIndex))
        }
    }
}
