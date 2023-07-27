package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.text.Text
import org.c0nstexpr.fishology.core.chat
import org.c0nstexpr.fishology.core.events.HookedEvent
import org.c0nstexpr.fishology.core.modId
import org.c0nstexpr.fishology.logger

class HookedInteraction(
    val client: MinecraftClient,
    val enableChat: Boolean
) : DisposableScope by DisposableScope() {
    init {
        HookedEvent.observable.filter { it.bobber.id == bobber?.id }
            .subscribeScoped { entity = it.hooked }
    }

    var bobber: FishingBobberEntity? = null
        set(value) {
            entity = null
            field = value
        }

    var entity: Entity? = null
        private set(value) {
            if (value != null && enableChat) client.chat(
                Text.translatable("${modId}.caught_on_chat")
                    .append(value.displayName)
                    .string,
                logger
            )

            field = value
        }
}
