package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.doOnAfterSubscribe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.doOnAfterSubscribe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.text.Text
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame

class DiscardLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    var lootsFilter: Set<FishingLoot> = setOf(),
) : SwitchDisposable() {
    private var notified = false
    private val player get() = rod.player

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        notified = false

        return caught.filter { lootsFilter.contains(it.stack.getLoot()) }
            .map { it.stack.copy() }
            .switchMapMaybe(::onCaughtExcluded)
            .subscribe { (player ?: return@subscribe).dropSelectedItem(false) }
    }

    private fun onCaughtExcluded(stack: ItemStack) = SlotUpdateEvent.observable
        .filter { validateSelectedSlot() && it.stack.isSame(stack) }
        .mapNotNull {
            when (it.syncId) {
                ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID -> it.slot
                0 -> player?.playerScreenHandler?.getSlot(it.slot)?.index
                else -> null
            }
        }
        .firstOrComplete()
        .flatMap { onSlotUpdate(it, stack) }

    private fun onSlotUpdate(slot: Int, stack: ItemStack) = SelectedSlotUpdateEvent.observable
        .filter { stack.isSame(player?.inventory?.mainHandStack) }
        .firstOrComplete()
        .doOnAfterSubscribe { rod.client.interactionManager?.pickFromInventory(slot) }

    private fun validateSelectedSlot() = rod.run {
        logger.d("detected excluded loots")

        player?.let {
            if (rodItem?.slotIndex == it.inventory.selectedSlot) {
                logger.d("rod is selected, aborting")

                if (!notified) {
                    client.msg(Text.translatable("$modId.excluded_loots_notification"))
                    notified = true
                }

                false
            } else {
                true
            }
        } ?: false
    }
}
