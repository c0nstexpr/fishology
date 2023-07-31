package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.SerialDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.text.Text
import org.c0nstexpr.fishology.core.chat
import org.c0nstexpr.fishology.core.events.BobberOwnedEvent
import org.c0nstexpr.fishology.core.events.HookedEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId

class BobberInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    var bobber: FishingBobberEntity? = null
        private set

    private val hookSubject = BehaviorSubject(null as Entity?)

    val hook: BehaviorObservable<Entity?> = hookSubject

    private var chatDisposable = SerialDisposable()

    var enableChat = false
        set(value) {
            if (value == field) return

            if (value) {
                chatDisposable.set(
                        hook.notNull().subscribe {
                            client.chat(
                                    Text.translatable("$modId.caught_on_chat")
                                            .append(it.displayName)
                                            .string,
                                    logger,
                            )
                        },
                )
            }

            field = value
        }

    init {
        BobberOwnedEvent.observable.map { it.bobber }.filter {
            client.player?.run { it.playerOwner?.id == id } == true
        }.subscribeScoped { bobber = it }

        HookedEvent.observable.filter { it.bobber.uuid == bobber?.uuid }
                .subscribeScoped { hookSubject.onNext(it.hook) }

        chatDisposable.scope()
    }
}
