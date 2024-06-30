package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.doOnAfterSubscribe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.flatMapObservable
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOf
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.maybe.observeOn
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.maybe.timeout
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.doOnAfterSubscribe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
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
import org.c0nstexpr.fishology.utils.MCClientScheduler
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.fishHookRemovedObservable
import org.c0nstexpr.fishology.utils.swapHand
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

class AutoFishing(private val rod: Rod, private val loot: Observable<Loot>) : SwitchDisposable() {
    var recastThreshold = 3.seconds

    private val clientScheduler = MCClientScheduler(rod.client)

    override fun onEnable(): Disposable {
        logger.d<AutoFishing> { "enable auto fishing" }

        return rod.itemObservable.filter { it.isThrow }
            .switchMap { rodItem ->
                CaughtFishEvent.observable.filter { it.caught }.switchMap { onCaughtFish(rodItem) }
            }
            .subscribe { }
    }

    private fun onCaughtFish(rodItem: RodItem): Observable<Unit> = loot.map { it.entity }
        .firstOrComplete()
        .let {
            if (recastThreshold.isFinite()) it.timeout(
                recastThreshold,
                ioScheduler,
                swapHands(rodItem.player).observeOn(clientScheduler)
                    .map {
                        logger.d<AutoFishing> { "recast threshold $recastThreshold reached" }
                        null
                    }
            )
            else it
        }
        .flatMapObservable {
            onGetLoot(it).flatMapObservable { player ->
                if (recast()) onRecast(player) else observableOfEmpty()
            }
        }
        .doOnAfterSubscribe {
            logger.d<AutoFishing> { "retrieve rod" }
            rod.use()
        }

    private fun onGetLoot(loot: ItemEntity?): Maybe<ClientPlayerEntity> {
        val player = rod.player

        if (player == null) {
            logger.w<AutoFishing> { "player is null" }
            return maybeOfEmpty()
        }

        if (loot == null) return maybeOf(player)

        fun isSameItem(it: ItemEntity) = it.id == loot.id

        // observe the caught entity dropping or removed
        return merge(
            ItemEntityVelEvent.observable.map { it.entity }.filter {
                isSameItem(it) &&
                    vecComponents.any { abs(it(loot.velocity)) <= 0.1 } &&
                    loot.pos.y < player.eyeY - 1
            },
            ItemEntityRemoveEvent.observable.map { it.entity }.filter(::isSameItem)
        ).firstOrComplete().map { player }
    }

    private val recastSubject = PublishSubject<Unit>()

    private val serialDisposable = SerialDisposable().scope()

    private fun onRecast(player: ClientPlayerEntity): Observable<Unit> {
        var retryCount = 0u

        fun observeRecastFailed() = serialDisposable.set(
            merge(
                HookedEvent.observable.filter { it.hook != null },
                EntityOnGroundEvent.observable.filter { it.onGround }
                    .mapNotNull { it.entity as? FishingBobberEntity }
                    .filter { it.id == player.fishHook?.id }
            ).firstOrComplete().subscribe {
                logger.d<AutoFishing> {
                    "recast for ${++retryCount} times failed, bobber is on ground or hooked entity"
                }
                recastSubject.onNext(Unit)
            }
        )

        return recastSubject.switchMapMaybe {
            swapHands(player).map { if (recast()) observeRecastFailed() }
                .doOnAfterSubscribe { observeRecastFailed() }
        }
    }

    private fun swapHands(player: ClientPlayerEntity): Maybe<Unit> {
        logger.d<AutoFishing> { "try to discard bobber" }

        val rodItem = rod.rodItem.takeIf { it?.isValid() == true } ?: return maybeOfEmpty()
        val inv = player.inventory
        val network = player.networkHandler
        val selected = inv.selectedSlot
        val offHandStack = player.offHandStack.copy()
        val handler = player.playerScreenHandler

        val slotObservable = SlotUpdateEvent.observable.filter {
            it.syncId == handler.syncId && ItemStack.areEqual(it.stack, offHandStack)
        }
            .map { handler.getSlot(it.slot).index }

        if (rodItem.slotIndex == selected) return scrollHotBar(inv, network)

        return slotObservable.filter { it == selected }
            .firstOrComplete()
            .flatMap {
                scrollHotBar(inv, network)
                    .flatMap {
                        slotObservable.filter { it == PlayerInventory.OFF_HAND_SLOT }
                            .map { }
                            .firstOrComplete()
                            .doOnAfterSubscribe { player.swapHand() }
                    }
            }
            .doOnAfterSubscribe { player.swapHand() }
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

        return fishHookRemovedObservable().firstOrComplete().map {
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
