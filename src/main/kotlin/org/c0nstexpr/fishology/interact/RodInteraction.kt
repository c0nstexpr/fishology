@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.UseRodEvent

class RodInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    init {
        UseRodEvent.afterUse.subscribeScoped(onNext = ::onAfterUseRod)
        UseRodEvent.beforeUse.subscribeScoped(onNext = ::onBeforeUseRod)
    }

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

    fun use() = client.interactionManager?.interactItem(client.player, hand)
}
