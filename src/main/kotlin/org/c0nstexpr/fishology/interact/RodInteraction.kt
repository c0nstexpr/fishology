@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import net.minecraft.client.MinecraftClient
import net.minecraft.item.FishingRodItem
import net.minecraft.item.ItemStack.areEqual
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.ItemCoolDownEvent
import org.c0nstexpr.fishology.core.events.UseRodEvent
import org.c0nstexpr.fishology.utils.execute
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class RodInteraction(val client: MinecraftClient) : CoroutineScope by CoroutineScope(dispatcher)  {
    val dispatcher = client.asCoroutineDispatcher()

    val beforeUse = UseRodEvent.beforeUseFlow.filter { isSamePlayer(it) }

    val afterUse = UseRodEvent.afterUseFlow.filter { isSamePlayer(it) }

    val player get() = client.player

    private fun isSamePlayer(arg: UseRodEvent.Arg?) =
        (player != null) && (arg?.player?.uuid == player?.uuid)

    private val mutableHand = MutableStateFlow<Hand?>(null)

    val hand get() = mutableHand.value

    private val job = CoroutineScope(dispatcher).launch {
        beforeUse.collect {
            mutableHand.emit(it?.hand)
        }
    }

    suspend fun use() {
        val player = player ?: return
        val itemStack = player.getStackInHand(hand)
        val interact = { client.interactionManager?.interactItem(player, hand) }

        if (!player.itemCooldownManager.isCoolingDown(itemStack.item)) {
            interact()
            return
        }

        ItemCoolDownEvent.flow
            .filter { coolDowned ->
                val stackInHand = player.getStackInHand(hand)

                coolDowned.item is FishingRodItem &&
                    itemStack != null &&
                    stackInHand != null &&
                    areEqual(itemStack, stackInHand)
            }
            .collect { withContext(dispatcher) { interact() } }
    }

    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
}

