@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.SingleCallbacks
import com.badoo.reaktive.single.single
import com.badoo.reaktive.subject.publish.PublishSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.minecraft.client.MinecraftClient
import net.minecraft.item.FishingRodItem
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.ItemCoolDownEvent
import org.c0nstexpr.fishology.core.events.UseRodEvent

class RodInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    init {
        ItemCoolDownEvent.observable.filter { it.item is FishingRodItem }
            .subscribeScoped {
                if (equals(player?.getStackInHand(hand)))
                    coolDownSubject?.onSuccess(Unit)

            }
        UseRodEvent.afterUse.subscribeScoped(onNext = ::onAfterUseRod)
        UseRodEvent.beforeUse.subscribeScoped(onNext = ::onBeforeUseRod)
    }

    private var coolDownSubject: SingleCallbacks<Unit>? = null

    private val beforeSubject = PublishSubject<RodInteraction>()

    val beforeUse: Observable<RodInteraction> get() = beforeSubject

    private val afterSubject = PublishSubject<RodInteraction>()

    val afterUse: Observable<RodInteraction> get() = afterSubject

    var itemStack: ItemStack? = null
        private set

    var hand: Hand? = null
        private set

    val player get() = client.player

    private fun onAfterUseRod(arg: UseRodEvent.Arg) {
        if (!isSamePlayer(arg)) return
        beforeSubject.onNext(this)
    }

    private fun onBeforeUseRod(arg: UseRodEvent.Arg) {
        if (!isSamePlayer(arg)) return
        hand = arg.hand
        itemStack = player!!.getStackInHand(hand)

        afterSubject.onNext(this)
    }

    private fun isSamePlayer(arg: UseRodEvent.Arg) = arg.player.uuid == player?.uuid

    suspend fun use() {
        if (player?.itemCooldownManager?.isCoolingDown(itemStack?.item) == false) {
            coolDownSubject = single { }
        }

        client.interactionManager?.interactItem(player, hand)
    }
}
