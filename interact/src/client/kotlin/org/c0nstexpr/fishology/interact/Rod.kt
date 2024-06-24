package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.interactItem

class Rod(val client: MinecraftClient) : SwitchDisposable() {
    private val itemSubject = BehaviorSubject(RodItem())

    val itemObservable: Observable<RodItem> = itemSubject

    val rodItem get() = itemSubject.value

    val player get() = client.player

    val bobber get() = player?.fishHook

    override fun onEnable(): Disposable {
        logger.d<Rod> { "enable rod interaction" }
        return UseRodEvent.observable.filter { it.player.uuid == client.player?.uuid }.subscribe {
            logger.d<Rod> { "detected rod use, saving rod status" }
            itemSubject.onNext(RodItem(it.hand, it.player, it.isThrow))
        }
    }

    fun use(): Boolean {
        val p = player

        if (!rodItem.isValid(p)) {
            logger.d<Rod> { "invalid rod item in ${rodItem.hand}, aborting" }
            return false
        }

        val hand = rodItem.hand

        client.interactItem(hand)
        p!!.swingHand(hand)

        return true
    }
}
