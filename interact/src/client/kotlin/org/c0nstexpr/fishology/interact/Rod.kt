package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.interactItem

class Rod(val client: MinecraftClient) : SwitchDisposable() {
    private val itemSubject = BehaviorSubject(null as RodItem?)

    val item: BehaviorObservable<RodItem?> = itemSubject

    override fun onEnable(): Disposable {
        logger.d("enable rod interaction")
        return UseRodEvent.beforeUseObservable.filter { it.player.uuid == client.player?.uuid }
            .subscribe {
                logger.d("detected rod use, saving rod status")
                itemSubject.onNext(it.let { RodItem(it.hand, it.player) })
            }
    }

    fun use() = itemSubject.value.let {
        if (it == null) {
            logger.d("no rod item, aborting")
            return@use false
        } else {
            it
        }
    }.run {
        if (!equals(RodItem(hand, player))) {
            logger.d("rod status not match, aborting")
            return@run false
        }

        client.execute {
            logger.d("use rod")
            client.interactItem(hand)
            player.swingHand(hand)
        }

        return@run true
    }
}
