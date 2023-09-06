package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.zip
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
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

    private class CaughtItemSlot(
        val player: ClientPlayerEntity,
        val slot: Int,
        val stack: ItemStack
    ) {
        fun pick(manager: ClientPlayerInteractionManager) {
            if (stack.isSame(stack)) manager.pickFromInventory(slot)
        }

        fun drop() = player.run {
            if (dropSelectedItem(false)) {
                logger.d("dropped excluded caught item")
                swingHand(Hand.MAIN_HAND)
            } else {
                logger.w("failed to drop excluded caught item")
            }
        }
    }

    private val discardSubject = PublishSubject<CaughtItemSlot>()

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        notified = false

        discardSubject.subscribe {
            if (!it.player.validateSelectedSlot()) return@subscribe

            val manager = rod.client.interactionManager ?: run {
                logger.w("interaction manager is null")
                return@subscribe
            }

            it.pick(manager)
        }

        observableStep(discardSubject).concatMaybe(
            {
                val inventory = player.inventory

                val slotUpdateObservable =
                    SlotUpdateEvent.observable.filter { it.syncId == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID }

                zip(
                    slotUpdateObservable.filter { it.slot == slot }
                        .map { logger.d("inventory slot update") },
                    slotUpdateObservable.filter { it.slot == inventory.selectedSlot }
                        .map { logger.d("selected slot update") },
                    SelectedSlotUpdateEvent.observable.filter {
                        stack.isSame(inventory.getStack(it.slot))
                    }
                        .map { this },
                ) { _, _, player -> player }
                    .firstOrComplete()
            }
        ).subscribe {
            it.drop()
        }

        return observableStep(
            caught.filter { lootsFilter.contains(it.stack.getLoot()) }.mapNotNull {
                rod.player.let { p ->
                    if (p == null || p.inventory.getEmptySlot() == -1) null
                    else Loot(p, it.stack.copy())
                }
            },
        )
            .concatMaybe(
                {
                    SlotUpdateEvent.observable.mapNotNull { mapSlopUpdate(it) }.firstOrComplete()
                }) { discardSubject.onNext(this) }

//            .concatMaybe(
//                {
//                    ClientTickEvent.observable.firstOrComplete().map {
//                        logger.d("client ticked")
//                        this
//                    }
//                },
//            )
            .tryOn()
            .subscribe { }
    }

    private fun Loot.mapSlopUpdate(arg: SlotUpdateEvent.Arg): CaughtItemSlot? {
        return if (arg.stack.isSame(stack) && arg.syncId == player.playerScreenHandler.syncId) {
            CaughtItemSlot(
                player,
                player.playerScreenHandler.getSlot(arg.slot).index,
                stack
            )
        } else {
            logger.w("client player is null")
            null
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

    companion object {
        private data class Loot(
            val player: ClientPlayerEntity,
            val stack: ItemStack,
        )
    }
}
