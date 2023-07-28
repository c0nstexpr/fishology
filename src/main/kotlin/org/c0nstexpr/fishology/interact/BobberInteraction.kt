package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.FishingBobberEntity
import org.c0nstexpr.fishology.core.events.BobberOwnedEvent
import org.c0nstexpr.fishology.core.events.HookedEvent

class BobberInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    private val bobberSubject = BehaviorSubject(null as FishingBobberEntity?)

    var bobber: Observable<FishingBobberEntity?> = bobberSubject

    private val entitySubject = BehaviorSubject(null as Entity?)

    val entity: Observable<Entity?> = entitySubject

    init {
        BobberOwnedEvent.observable.map { it.bobber }.filter {
            client.player?.run { it.playerOwner?.id == id } == true
        }.subscribeScoped(onNext = bobberSubject::onNext)

        HookedEvent.observable.filter { it.bobber.id == bobberSubject.value?.id }
                .subscribeScoped { entitySubject.onNext(it.hooked) }
    }
}
