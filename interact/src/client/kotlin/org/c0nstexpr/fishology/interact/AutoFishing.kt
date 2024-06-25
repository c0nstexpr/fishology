package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOf
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.observable.zip
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.EntityOnGroundEvent
import org.c0nstexpr.fishology.events.EntityRemoveEvent
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.swapHand
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.abs

class AutoFishing(private val rod: Rod, private val loot: Observable<Loot>) : SwitchDisposable() {
    override fun onEnable() = CaughtFishEvent.observable.filter { it.caught }
        .switchMapMaybe {
            logger.d<AutoFishing> { "retrieve rod" }
            rod.use()

            loot.map { it.entity }.firstOrComplete()
        }
        .switchMapMaybe switch@{ caught ->
            if (caught == null)
                return@switch maybeOf<ItemEntity?>(null)

            val player = rod.player

            if (player == null) {
                logger.w<AutoFishing> { "player is null" }
                return@switch maybeOfEmpty()
            }

            fun isSameItem(it: ItemEntity) = it.id == caught.id

            // observe the caught entity dropping or removed
            merge(
                ItemEntityVelEvent.observable.map { it.entity }
                    .filter {
                        isSameItem(it) &&
                            (
                                vecComponents.any { abs(it(caught.velocity)) <= 0.1 } ||
                                    caught.pos.y < player.pos.y + 1
                            )
                    },
                ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem)
            ).firstOrComplete()
        }
        .switchMap {
            recast()

            merge(
                HookedEvent.observable.filter { it.hook != null }.map { it.bobber },
                EntityOnGroundEvent.observable.filter { it.onGround }
                    .mapNotNull { it.entity as? FishingBobberEntity }
                    .filter { it.id == rod.bobber?.id }
            )
        }
        .switchMapMaybe switch@{ bobber ->
            val rodItem = rod.rodItem ?: return@switch maybeOfEmpty()
            val player = rodItem.player
            val inv = player.inventory
            val bobberId = bobber.id
            val network = player.networkHandler

            if (rodItem.slotIndex == inv.selectedSlot)
                return@switch scrollHotBar(inv, bobberId, network)

            val mainHandStack = inv.mainHandStack.copy()
            val offHandStack = inv.offHand.first().copy()
            val screenHandler = player.playerScreenHandler

            val slotObservable =
                SlotUpdateEvent.observable.filter { it.syncId == screenHandler.syncId }
                    .map { screenHandler.getSlot(it.slot) }
            val swapObservable = zip(
                slotObservable.filter {
                    it.index == PlayerInventory.OFF_HAND_SLOT &&
                        ItemStack.areEqual(it.stack, mainHandStack)
                },
                slotObservable.filter {
                    it.index == inv.selectedSlot && ItemStack.areEqual(it.stack, offHandStack)
                }
            ) { _ -> }.firstOrComplete()

            player.swapHand()

            return@switch swapObservable.flatMap { scrollHotBar(inv, bobberId, network) }
                .flatMap {
                    player.swapHand()
                    swapObservable
                }
        }
        .subscribe { recast() }

    private fun scrollHotBar(
        inv: PlayerInventory,
        bobberId: Int,
        network: ClientPlayNetworkHandler
    ): Maybe<Unit> {
        val swappable = inv.run {
            for (i in 0..8) {
                val j = (selectedSlot + i) % 9
                val stack = inv.main[j]
                if (!stack.isOf(Items.FISHING_ROD)) return@run j
            }

            -1
        }

        if (swappable == -1) {
            logger.d<AutoFishing> { "no swappable slot found" }
            return maybeOfEmpty()
        }

        val selected = inv.selectedSlot

        inv.selectedSlot = swappable
        network.sendPacket(UpdateSelectedSlotC2SPacket(swappable))

        return EntityRemoveEvent.observable.filter { bobberId == it.entity.id }
            .firstOrComplete()
            .map {
                inv.selectedSlot = selected
                network.sendPacket(UpdateSelectedSlotC2SPacket(selected))
            }
    }

    private fun recast() {
        logger.d<AutoFishing> { "recast rod" }
        if (!rod.use()) logger.d<AutoFishing> { "recast not success" }
    }
}
