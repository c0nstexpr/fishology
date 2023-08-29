package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.utils.SwitchDisposable

class HookChat(val client: MinecraftClient) : SwitchDisposable() {
    private fun Entity.chat() = client.chat(
        Text.translatable("$modId.hooked_on_chat").append(displayName).string,
        logger,
    )

    override fun onEnable(): Disposable {
        logger.d("enable hook chat interaction")
        return HookedEvent.observable.filter {
            client.player?.run { it.bobber.owner?.uuid == uuid } ?: false
        }
            .map { it.hook }
            .subscribe { it.chat() }
    }
}
