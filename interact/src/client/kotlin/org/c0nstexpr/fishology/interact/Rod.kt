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

    val itemObservable: Observable<RodItem> = itemSubject.notNull().filter { !isUsing }

    val rodItem get() = itemSubject.value

    val player get() = rodItem?.player

    val bobber get() = player?.fishHook

    private var isUsing = false

    override fun onEnable(): Disposable {
        logger.d<Rod> { "enable rod interaction" }
        return UseRodEvent.observable.subscribe {
            logger.d<Rod> { "save rod status after use rod" }
            itemSubject.onNext(RodItem(it.player, it.isThrow, it.hand))
        }
    }

    fun use(): Boolean {
        val item = rodItem

        if (item?.isValid() != true) {
            logger.d<Rod> { "invalid rod item ${item?.run { "in $hand" }}, aborting" }
            return false
        }

        isUsing = true

        val hand = item.hand

        client.interactItem(hand)
        item.player.swingHand(hand)

        isUsing = false

        return true
    }
}
