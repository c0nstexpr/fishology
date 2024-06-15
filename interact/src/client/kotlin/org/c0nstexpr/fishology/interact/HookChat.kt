package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger

class HookChat(client: MinecraftClient) : ChatInteraction(client, "hooked_on_chat") {
    override fun onEnable(): Disposable {
        logger.d<HookChat> { "enable hook chat interaction" }
        return HookedEvent.observable.filter {
            client.player?.run { it.bobber.owner?.id == id } ?: false
        }
            .mapNotNull { it.hook?.displayName }
            .subscribe { notify(it) }
    }
}
