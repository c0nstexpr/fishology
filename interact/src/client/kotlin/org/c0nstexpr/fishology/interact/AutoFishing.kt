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
import com.badoo.reaktive.observable.observableOfEmpty
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
import org.c0nstexpr.fishology.events.CooldownEvent
import org.c0nstexpr.fishology.events.EntityOnGroundEvent
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityRemoveEvent
import org.c0nstexpr.fishology.events.ItemEntityVelEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.fishHookRemovedObservable
import org.c0nstexpr.fishology.utils.swapHand
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.abs

class AutoFishing(private val rod: Rod, private val loot: Observable<Loot>) : SwitchDisposable() {
    private var isRecast: Boolean = false

    private fun rodCooldownMaybe() = if (rod.isCooldown == true)
        CooldownEvent.observable.filter { it.cooldown == 0 && it.item == Items.FISHING_ROD }
            .firstOrComplete()
    else maybeOfEmpty()

    override fun onEnable() = rod.itemObservable.filter { it.isThrow }
        .switchMap {
            merge()
            CaughtFishEvent.observable.filter { it.caught }
        }
        .switchMapMaybe {
            logger.d<AutoFishing> { "retrieve rod" }
            rod.use()

            loot.map { it.entity }.firstOrComplete()
        }
        .switchMapMaybe(::lootMaybe)
        .switchMap { recastObservable() }
        .subscribe { recast() }

    private fun lootMaybe(loot: ItemEntity?): Maybe<ItemEntity?> {
        if (loot == null)
            return maybeOf<ItemEntity?>(null)

        val player = rod.player

        if (player == null) {
            logger.w<AutoFishing> { "player is null" }
            return maybeOfEmpty()
        }

        fun isSameItem(it: ItemEntity) = it.id == loot.id

        // observe the caught entity dropping or removed
        return merge(
            ItemEntityVelEvent.observable.map { it.entity }
                .filter {
                    isSameItem(it) &&
                        vecComponents.any { abs(it(loot.velocity)) <= 0.1 } &&
                        loot.pos.y < player.pos.y - 0.5
                },
            ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem)
        ).firstOrComplete()
    }

    private fun recastObservable(): Observable<CooldownEvent.Arg> {
        if (!recast()) return observableOfEmpty()

        return merge(
            HookedEvent.observable.filter { it.hook != null },
            EntityOnGroundEvent.observable.filter { it.onGround }
                .mapNotNull { it.entity as? FishingBobberEntity }
                .filter { it.id == rod.player?.fishHook?.id }
        )
            .switchMapMaybe switch@{
                val rodItem = rod.rodItem ?: return@switch maybeOfEmpty()
                val player = rodItem.player
                val inv = player.inventory
                val network = player.networkHandler

                if (rodItem.slotIndex == inv.selectedSlot)
                    return@switch scrollHotBar(inv, network)

                val mainHandStack = inv.mainHandStack.copy()
                val offHandStack = inv.offHand.first().copy()
                val screenHandler = player.playerScreenHandler

                val slotObservable = SlotUpdateEvent.observable
                    .filter { it.syncId == screenHandler.syncId }
                    .map { screenHandler.getSlot(it.slot) }
                val swapObservable = zip(
                    slotObservable.filter {
                        it.index == PlayerInventory.OFF_HAND_SLOT &&
                            ItemStack.areEqual(it.stack, mainHandStack)
                    },
                    slotObservable.filter {
                        it.index == inv.selectedSlot &&
                            ItemStack.areEqual(
                                it.stack,
                                offHandStack
                            )
                    }
                ) { _ -> }.firstOrComplete()

                player.swapHand()

                swapObservable.flatMap { scrollHotBar(inv, network) }
                    .flatMap {
                        player.swapHand()
                        swapObservable
                    }
            }
            .switchMapMaybe { rodCooldownMaybe() }
    }

    private fun scrollHotBar(inv: PlayerInventory, network: ClientPlayNetworkHandler): Maybe<Unit> {
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

        return fishHookRemovedObservable().firstOrComplete()
            .map { inv.selectedSlot = selected }
    }

    private fun recast(): Boolean {
        logger.d<AutoFishing> { "recast rod" }

        if (!rod.use()) {
            logger.d<AutoFishing> { "recast not success" }
            return false
        }

        return true
    }
}
