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
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.text.Text
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.events.BobberOwnedEvent
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntitySetVelocityEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import kotlin.math.absoluteValue
import kotlin.math.sqrt

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

        ItemEntitySetVelocityEvent.observable.map { it.entity }
            .filter(::isHookedItem)
            .subscribeScoped(onNext = hookSubject::onNext)

        chatDisposable.scope()
    }

    private fun isHookedItem(entity: ItemEntity): Boolean {
        if (hook.value?.uuid == entity.uuid) return true

        // bobber use code:
        // ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), itemStack2);
        // double d = playerEntity.getX() - this.getX();
        // double e = playerEntity.getY() - this.getY();
        // double f = playerEntity.getZ() - this.getZ();
        // double g = 0.1;
        // itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

        // bobber bobbing code:
        // setVelocity(vec3d.x * 0.9, vec3d.y - d * (double)this.random.nextFloat() * 0.2, vec3d.z * 0.9);

        entity.run {
            if (hook.value?.uuid == entity.uuid) return false

            val bobber = bobber ?: return false
            val bobberPos = bobber.pos

            if (
                (pos.x - bobberPos.x).absoluteValue > ERROR &&
                (pos.y - bobberPos.y).absoluteValue > ERROR &&
                (pos.z - bobberPos.z).absoluteValue > ERROR
            ) {
                return false
            }

            val playerPos = bobber.owner?.pos ?: return false
            val relativePosX = playerPos.x - bobberPos.x
            if ((relativePosX * G - velocity.x).absoluteValue > ERROR) return false

            val relativePosZ = playerPos.z - bobberPos.z
            if ((relativePosZ * G - velocity.z).absoluteValue > ERROR) return false

            val relativePosY = playerPos.y - bobberPos.y
            val d = relativePosX * relativePosX +
                    relativePosY * relativePosY +
                    relativePosZ * relativePosZ
            if ((relativePosY * G + sqrt(sqrt(d)) * 0.08 - velocity.y).absoluteValue > ERROR) {
                return false
            }

            return true
        }
    }

    companion object {
        private const val ERROR = 0.01
        private const val G = 0.1
    }
}
