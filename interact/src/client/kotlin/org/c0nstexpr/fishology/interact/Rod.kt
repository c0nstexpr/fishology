package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.interactItem

class Rod(val client: MinecraftClient) : SwitchDisposable() {
    private val itemSubject = BehaviorSubject(null as RodItem?)

    val itemObservable: Observable<RodItem?> = itemSubject

    val rodItem get() = itemSubject.value?.takeIf { it == RodItem(it.hand, it.player, it.inUse) }

    val player get() = client.player

    val bobber get() = player?.fishHook

    override fun onEnable(): Disposable {
        logger.d("enable rod interaction")
        return UseRodEvent.observable.filter { it.player.uuid == client.player?.uuid }
            .tryOn()
            .subscribe {
                logger.d("detected rod use, saving rod status")
                itemSubject.onNext(RodItem(it.hand, it.player, it.isThrow))
            }
    }

    fun use() = rodItem.run {
        if (this == null) {
            logger.d("no rod item, aborting")
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
