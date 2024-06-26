package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.flatMapObservable
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
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import org.c0nstexpr.fishology.events.CaughtFishEvent
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

    override fun onEnable() = rod.itemObservable.filter { it.isThrow }
        .switchMap {
            CaughtFishEvent.observable.filter { it.caught }.switchMap {
                logger.d<AutoFishing> { "retrieve rod" }
                rod.use()
                loot.map { it.entity }.firstOrComplete().flatMapObservable {
                    lootMaybe(it).flatMapObservable { player ->
                        if (recast()) recastObservable(player) else observableOfEmpty()
                    }
                }
            }
        }
        .subscribe { }

    private fun lootMaybe(loot: ItemEntity?): Maybe<ClientPlayerEntity> {
        val player = rod.player

        if (player == null) {
            logger.w<AutoFishing> { "player is null" }
            return maybeOfEmpty()
        }

        if (loot == null) return maybeOf(player)

        fun isSameItem(it: ItemEntity) = it.id == loot.id

        // observe the caught entity dropping or removed
        return merge(
            ItemEntityVelEvent.observable.map { it.entity }
                .filter {
                    isSameItem(it) &&
                        vecComponents.any { abs(it(loot.velocity)) <= 0.1 } &&
                        loot.pos.y < player.eyeY - 0.5
                },
            ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem)
        ).firstOrComplete().map { player }
    }

    private fun recastObservable(player: ClientPlayerEntity): Observable<Unit> {
        var retryCount = 0u
        var retrying = false

        return merge(
            HookedEvent.observable.filter { it.hook != null },
            EntityOnGroundEvent.observable.filter { it.onGround }
                .mapNotNull { it.entity as? FishingBobberEntity }
                .filter { it.id == player.fishHook?.id }
        )
            .filter { !retrying }
            .switchMapMaybe {
                logger.d<AutoFishing> {
                    "recast for ${++retryCount} times failed, bobber is on ground or hooked entity"
                }

                retrying = true

                recastFailedObservable(player).map {
                    recast()
                    retrying = false
                }
            }
    }

    private fun recastFailedObservable(player: ClientPlayerEntity): Maybe<Any> {
        val rodItem = rod.rodItem.takeIf { it?.isValid() == true } ?: return maybeOfEmpty()
        val inv = player.inventory
        val network = player.networkHandler
        val selected = inv.selectedSlot

        if (rodItem.slotIndex == selected) return scrollHotBar(inv, network)

        val offHandStack = player.offHandStack.copy()
        val screenHandler = player.playerScreenHandler
        val slotObservable = SlotUpdateEvent.observable.filter {
            it.syncId == screenHandler.syncId && ItemStack.areEqual(it.stack, offHandStack)
        }
            .map { screenHandler.getSlot(it.slot).index }

        player.swapHand()

        return slotObservable.filter { it == selected }
            .firstOrComplete()
            .flatMap {
                scrollHotBar(inv, network).flatMap {
                    player.swapHand()
                    slotObservable.filter { it == PlayerInventory.OFF_HAND_SLOT }.firstOrComplete()
                }
            }
    }

    private fun scrollHotBar(inv: PlayerInventory, network: ClientPlayNetworkHandler): Maybe<Unit> {
        val swappable = inv.run {
            for (i in 0..8) {
                val j = (selectedSlot + i) % 9
                val stack = inv.main[j]
                if (!stack.isOf(Items.FISHING_ROD)) return@run j
            }

            logger.d<AutoFishing> { "no swappable slot found" }
            return maybeOfEmpty()
        }

        val selected = inv.selectedSlot

        inv.selectedSlot = swappable
        network.sendPacket(UpdateSelectedSlotC2SPacket(swappable))

        return fishHookRemovedObservable()
            .firstOrComplete()
            .map {
                inv.selectedSlot = selected
                network.sendPacket(UpdateSelectedSlotC2SPacket(selected))
            }
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
