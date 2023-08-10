package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.interactItem

class Rod(val client: MinecraftClient) : SwitchDisposable() {
    private var item: RodItem? = null

    override fun onEnable(): Disposable {
        logger.d("enable rod interaction")
        return UseRodEvent.beforeUseObservable.filter { it.player.uuid == client.player?.uuid }
            .subscribe {
                logger.d("detected rod use, saving rod status")
                item = it.let { RodItem(it.hand, it.player) }
            }
    }

    fun use(): Boolean {
        val item = item.let {
            if (it == null) {
                logger.d("no rod item, aborting")

                return false
            } else {
                it
            }
        }

        logger.d("using rod")

        if (item != RodItem(item.hand, item.player)) {
            logger.d("rod status not match, aborting")
            return false
        }

        client.execute {
            logger.d("use rod")
            client.interactItem(item.hand)
        }

        return true
    }
}
