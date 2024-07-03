package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.doOnAfterSubscribe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.flatMapObservable
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOf
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.maybe.timeout
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.doOnAfterSubscribe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapMaybe
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
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.clientScheduler
import org.c0nstexpr.fishology.utils.fishHookRemovedObservable
import org.c0nstexpr.fishology.utils.swapHand
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

class AutoFishing(private val rod: Rod, private val loot: Observable<ItemEntity>) :
    SwitchDisposable() {
    var recastThreshold = 3.seconds
        set(value) {
            logger.d<AutoFishing> { "recast threshold set to $value" }
            field = value
        }

    private val clientScheduler = clientScheduler()

    init {
        doOnDispose { clientScheduler.destroy() }
    }

    override fun onEnable(): Disposable {
        logger.d<AutoFishing> { "enable auto fishing" }

        return rod.itemObservable.filter { it.isThrow }
            .switchMap { rodItem ->
                CaughtFishEvent.observable.filter { it.caught }.switchMap { onCaughtFish(rodItem) }
            }
            .subscribe { }
    }

    private fun onCaughtFish(rodItem: RodItem): Observable<Unit> = loot.firstOrComplete()
        .let {
            if (recastThreshold.isFinite()) it.timeout(
                recastThreshold,
                clientScheduler,
                resetRodStatus(rodItem.player).doOnAfterSubscribe {
                    logger.d<AutoFishing> { "recast threshold $recastThreshold reached" }
                }
                    .map { null }
            )
            else it
        }
        .flatMapObservable {
            onGetLoot(it).flatMapObservable { player ->
                onRecast(player).doOnAfterSubscribe { recast() }
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

        fun setRecastEvent() = serialDisposable.set(
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
            resetRodStatus(player).map { if (recast()) setRecastEvent() }
        }
            .doOnAfterSubscribe { setRecastEvent() }
    }

    private fun resetRodStatus(player: ClientPlayerEntity): Maybe<Unit> {
        val rodItem = rod.rodItem.takeIf { it?.isValid() == true } ?: return maybeOfEmpty()
        val inv = player.inventory
        val network = player.networkHandler
        var selected = inv.selectedSlot

        if (rodItem.slotIndex == selected) return scrollHotBar(inv, network)
            .doOnAfterSubscribe { logger.d<AutoFishing> { "try to reset rod status" } }

        scrollToNonRodSlot(inv, network)

        if (player.mainHandStack.isOf(Items.FISHING_ROD)) {
            logger.d<AutoFishing> { "abort reset, still have rod in main hand" }
            return maybeOfEmpty()
        }

        val stack = player.offHandStack.copy()
        val handler = player.playerScreenHandler
        val observable = SlotUpdateEvent.observable.filter {
            it.syncId == handler.syncId && ItemStack.areEqual(it.stack, stack)
        }
            .map { handler.getSlot(it.slot).index }

        selected = inv.selectedSlot
        return observable.filter { it == selected }
            .firstOrComplete()
            .flatMap {
                scrollHotBar(inv, network).flatMap {
                    observable.filter { it == PlayerInventory.OFF_HAND_SLOT }
                        .firstOrComplete()
                        .doOnAfterSubscribe { player.swapHand() }
                        .map { }
                }
            }
            .doOnAfterSubscribe {
                logger.d<AutoFishing> { "try reset rod status" }
                player.swapHand()
            }
    }

    private fun scrollHotBar(inv: PlayerInventory, network: ClientPlayNetworkHandler): Maybe<Unit> {
        val selected = inv.selectedSlot

        return fishHookRemovedObservable().firstOrComplete().map {
            inv.selectedSlot = selected
            network.sendPacket(UpdateSelectedSlotC2SPacket(selected))
        }.doOnAfterSubscribe { scrollToNonRodSlot(inv, network) }
    }

    private fun scrollToNonRodSlot(inv: PlayerInventory, network: ClientPlayNetworkHandler) {
        val nonRodSlot = inv.run {
            for (i in 0..<PlayerInventory.getHotbarSize()) {
                val stack = inv.main[i]
                if (!stack.isOf(Items.FISHING_ROD)) return@run i
            }

            logger.d<AutoFishing> { "no suitable non-rod slot found" }

            return
        }

        logger.d<AutoFishing> { "scroll slot to $nonRodSlot" }

        inv.selectedSlot = nonRodSlot
        network.sendPacket(UpdateSelectedSlotC2SPacket(nonRodSlot))
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
