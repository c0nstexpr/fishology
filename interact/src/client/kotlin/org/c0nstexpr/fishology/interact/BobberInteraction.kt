package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.events.BobberOwnedEvent
import org.c0nstexpr.fishology.events.HookedEvent
import org.c0nstexpr.fishology.events.ItemEntityVelPacketEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class BobberInteraction(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    var bobber: FishingBobberEntity? = null
        private set

    private val hookSubject = PublishSubject<Entity>()

    val hook: Observable<Entity?> = hookSubject

    private val caughtSubject = BehaviorSubject(null as ItemEntity?)

    val caught: BehaviorObservable<ItemEntity?> = caughtSubject

    private var chatDisposable = CompositeDisposable()

    var enableChat = false
        set(value) {
            if (value == field) return

            if (value) {
                chatDisposable.run {
                    add(hook.notNull().subscribe { chat(it.displayName, "hooked_on_chat") })
                    add(
                        caught.notNull().subscribe {
                            val text = Text.empty().append(it.displayName)
                            EnchantmentHelper.get(it.stack)
                                .map { (enchantment, level) -> enchantment.getName(level) }
                                .forEach(text.append("\n")::append)
                            chat(text, "caught_on_chat")
                        },
                    )
                }
            } else {
                chatDisposable.clear(true)
            }

            field = value
        }

    private fun chat(text: Text, key: String) = client.chat(
        Text.translatable("$modId.$key").append(text).string,
        logger,
    )

    val caughtItemId get() = caughtSubject.value?.uuid

    init {
        BobberOwnedEvent.observable.map { it.bobber }
            .filter { client.player?.run { it.playerOwner?.id == id } == true }
            .subscribeScoped { bobber = it }

        HookedEvent.observable.filter { it.bobber.uuid == bobber?.uuid }
            .subscribeScoped { hookSubject.onNext(it.hook) }

        ItemEntityVelPacketEvent.observable.map { it.entity }
            .filter(::isCaughtItem)
            .subscribeScoped { caughtSubject.onNext(it) }

        chatDisposable.scope()
    }

    private fun isCaughtItem(entity: ItemEntity): Boolean = entity.run {
        if (caughtItemId == uuid) return false

        val bobber = bobber ?: return false
        if (
            nearBobber(bobber, Vec3d::x, Entity::prevX) &&
            nearBobber(bobber, Vec3d::y, Entity::prevY) &&
            nearBobber(bobber, Vec3d::z, Entity::prevZ)
        ) {
            return false
        }

        return isHookedVelocity(this, bobber)
    }

    // bobber use code:
    // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
    // double d = playerEntity.x - x;
    // double e = playerEntity.y - y;
    // double f = playerEntity.z - z;
    // double g = 0.1;
    // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
    private fun isHookedVelocity(entity: Entity, bobber: FishingBobberEntity): Boolean {
        entity.run {
            val playerPos = bobber.owner?.pos ?: return false
            val relativeX = playerPos.x - pos.x
            if ((relativeX * G - velocity.x).absoluteValue > ERROR) return false

            val relativeZ = playerPos.z - pos.z
            if ((relativeZ * G - velocity.z).absoluteValue > ERROR) return false

            val relativeY = playerPos.y - pos.y
            val d = relativeX * relativeX + relativeY * relativeY + relativeZ * relativeZ
            if ((relativeY * G + sqrt(sqrt(d)) * 0.08 - velocity.y).absoluteValue > ERROR) {
                return false
            }
        }

        return true
    }

    companion object {
        private const val ERROR = 0.01
        private const val G = 0.1

        private fun ItemEntity.nearBobber(
            bobber: FishingBobberEntity,
            getComponent: (Vec3d) -> Double,
            getPre: (Entity) -> Double,
        ): Boolean {
            val c = getComponent(bobber.pos)
            return absLess(getComponent(pos) - c, c - getPre(bobber), getComponent(bobber.velocity))
        }

        private fun absLess(a: Double, vararg b: Double) =
            b.all { a.absoluteValue < it.absoluteValue }
    }
}
