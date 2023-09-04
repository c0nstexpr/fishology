package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame
import org.c0nstexpr.fishology.utils.observableStep

class DiscardLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    var lootsFilter: Set<FishingLoot> = setOf(),
) : SwitchDisposable() {
    private var notified = false

    private data class CaughtItemSlot(
        val player: ClientPlayerEntity,
        val slot: Int,
        val stack: ItemStack,
    )

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        notified = false

        return observableStep(
            caught.filter { lootsFilter.contains(it.stack.getLoot()) }.map { it.stack.copy() },
        )
            .switchMaybe(
                {
                    SlotUpdateEvent.observable
                        .mapNotNull { mapSlopUpdate(it) }
                        .firstOrComplete()
                },
            ) {
                rod.client.interactionManager.let {
                    if (it == null) {
                        logger.w("interaction manager is null")
                        null
                    } else if (player.validateSelectedSlot()) {
                        it
                    } else {
                        null
                    }
                }?.pickFromInventory(slot)
            }
            .switchMaybe(
                {
                    SelectedSlotUpdateEvent.observable.filter { stack.isSame(player.inventory?.mainHandStack) }
                        .map { player }
                        .firstOrComplete()
                },
            ) {
                dropSelectedItem(false)
                swingHand(Hand.MAIN_HAND)
            }
            .tryOn()
            .subscribe { }
    }

    private fun ItemStack.mapSlopUpdate(it: SlotUpdateEvent.Arg) = rod.player.let { p ->
        if (p == null) {
            logger.w("client player is null")
            null
        } else {
            if (it.stack.isSame(this)) {
                when (it.syncId) {
                    ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID -> it.slot
                    0 -> p.playerScreenHandler?.run { getSlot(it.slot).index }
                        ?: this@DiscardLoot.run {
                            logger.w("player screen handler is null")
                            null
                        }

                    else -> null
                }
            } else {
                null
            }?.let { CaughtItemSlot(p, it, this) }
        }
    }

    private fun ClientPlayerEntity.validateSelectedSlot() = rod.run {
        logger.d("detected excluded loots")

        if (rodItem?.slotIndex == inventory.selectedSlot) {
            logger.d("rod is selected, aborting")

            if (!notified) {
                client.msg(Text.translatable("$modId.excluded_loots_notification"))
                notified = true
            }

            false
        } else {
            true
        }
    }
}
