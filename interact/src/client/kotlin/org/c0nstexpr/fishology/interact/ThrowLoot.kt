package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable

class ThrowLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    var lootsFilter: Set<FishingLoot> = setOf(),
) : SwitchDisposable() {
    override fun onEnable(): Disposable {
        var notified = false

        logger.d("enable throw loot interaction")

        return caught.map { it.stack }
            .filter { lootsFilter.contains(it.getLoot()) }
            .switchMap {
                val stack = it.copy()
                SlotUpdateEvent.observable.filter { arg ->
                    ItemStack.areEqual(arg.stack, stack)
                }
            }
            .subscribe {
                rod.run {
                    logger.d("detected excluded loots")

                    val p = player as? ClientPlayerEntity ?: return@subscribe

                    if (rodItem?.slotIndex == p.inventory.selectedSlot) {
                        logger.d("rod is selected, aborting")

                        if (!notified) {
                            client.msg(Text.translatable("$modId.excluded_loots_notification"))
                            notified = true
                        }
                        return@subscribe
                    }

                    client.interactionManager?.pickFromInventory(it.slot)
                    p.dropSelectedItem(false)
                }
            }
    }
}
