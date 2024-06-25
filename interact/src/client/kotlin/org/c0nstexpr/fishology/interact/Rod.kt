package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.interactItem

class Rod(val client: MinecraftClient) : SwitchDisposable() {
    private val itemSubject = BehaviorSubject<RodItem?>(null)

    val itemObservable: Observable<RodItem> = itemSubject.notNull()

    val rodItem get() = itemSubject.value

    val player get() = rodItem?.player

    val bobber get() = player?.fishHook

    override fun onEnable(): Disposable {
        logger.d<Rod> { "enable rod interaction" }
        return UseRodEvent.observable.filter { it.player.uuid == client.player?.uuid }.subscribe {
            logger.d<Rod> { "detected rod use, saving rod status" }
            itemSubject.onNext(RodItem(it.player, it.isThrow, it.hand))
        }
    }

    fun use(): Boolean {
        val item = rodItem

        if (item?.isValid() != true) {
            logger.d<Rod> { "invalid rod item ${item?.run { "in $hand" }}, aborting" }
            return false
        }

        val hand = item.hand

        client.interactItem(hand)
        item.player.swingHand(hand)

        return true
    }
}
