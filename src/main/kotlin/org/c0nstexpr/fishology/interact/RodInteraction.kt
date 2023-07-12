@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.MinecraftClient
import net.minecraft.item.FishingRodItem
import net.minecraft.util.Hand
import org.c0nstexpr.fishology.core.events.UseRodEvent
import org.c0nstexpr.fishology.utils.onNextOnce

class RodInteraction(val client: MinecraftClient) : Disposable {
    private var subscription = disposableScope {
        UseRodEvent.afterUse.subscribeScoped(onNext = ::onAfterUseRod)
        UseRodEvent.beforeUse.subscribeScoped(onNext = ::onBeforeUseRod)
    }

    private val beforeSubject = PublishSubject<RodInteraction>()

    val beforeUse: Observable<RodInteraction> get() = beforeSubject

    private val afterSubject = PublishSubject<RodInteraction>()

    val afterUse: Observable<RodInteraction> get() = afterSubject

    var item: FishingRodItem? = null
        private set

    var hand: Hand? = null
        private set

    val player get() = client.player

    private fun onAfterUseRod(arg: UseRodEvent.Arg) {
        if (arg.player.uuid != player?.uuid) return

        item = arg.item
        hand = arg.hand

        beforeSubject.onNextOnce(this)
    }

    private fun onBeforeUseRod(arg: UseRodEvent.Arg) {
        if (arg.player.uuid != player?.uuid) return

        afterSubject.onNextOnce(this)
    }

    fun use() = client.interactionManager?.interactItem(client.player, hand)

    override val isDisposed get() = subscription.isDisposed

    override fun dispose() = subscription.dispose()
}
