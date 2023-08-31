package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityRmovEvent
import org.c0nstexpr.fishology.events.ItemEntityVelPacketEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class CaughtFish(private val rod: Rod) : SwitchDisposable() {
    private val caughtSubject = PublishSubject<ItemEntity?>()

    val caught: Observable<ItemEntity?> = caughtSubject

    private val cacheEntity = HashMap<Int, UInt>()

    private val rodItemObservable get() = rod.itemObservable.notNull()

    override fun onEnable(): Disposable {
        logger.d("enable caught fish interaction")

        val rodUse = onRodCast()

        return disposableScope {
            rodItemObservable
                .filter { it.inUse }
                .switchMapMaybe { rodUse }
                .tryOn()
                .subscribeScoped {
                    logger.d("caught item: ${it.displayName}")
                    caughtSubject.onNext(it)
                }

            ItemEntityRmovEvent.observable.subscribeScoped {
                cacheEntity.remove(it.entity.id)
            }
        }
    }

    private fun onRodCast() =
        CaughtFishEvent.observable.filter { it.caught && it.bobber.id == rod.player?.fishHook?.id }
            .map { it.bobber }
            .firstOrComplete()
            .flatMap(::onCaughtFish)

    private fun onCaughtFish(bobber: FishingBobberEntity): Maybe<ItemEntity> {
        logger.d("caught fish")
        caughtSubject.onNext(null)

        val onRodRetrieve = onRodRetrieve(
            bobber.pos,
            Vec3d(bobber.prevX, bobber.prevY, bobber.prevZ),
            bobber.velocity,
            bobber.owner?.pos ?: return maybeOfEmpty(),
        )

        return rodItemObservable
            .filter { !it.inUse }
            .firstOrComplete()
            .flatMap { onRodRetrieve }
    }

    private fun onRodRetrieve(
        bobberPos: Vec3d,
        bobberPrevPos: Vec3d,
        bobberVelocity: Vec3d,
        playerPos: Vec3d,
    ) = ItemEntityVelPacketEvent.observable
        .map { it.entity }
        .filter { it.isCaughtItem(bobberPos, bobberPrevPos, bobberVelocity, playerPos) }
        .firstOrComplete()

    private fun Entity.isCaughtItem(
        bobberPos: Vec3d,
        bobberPrevPos: Vec3d,
        bobberVel: Vec3d,
        playerPos: Vec3d,
    ): Boolean {
        val count = cacheEntity.getOrDefault(id, 0u)

        cacheEntity[id] = count + 1u

        if (
            count > 0u ||
            !nearBobber(bobberPos, bobberPrevPos, bobberVel, Vec3d::x) ||
            !nearBobber(bobberPos, bobberPrevPos, bobberVel, Vec3d::y) ||
            !nearBobber(bobberPos, bobberPrevPos, bobberVel, Vec3d::z)
        ) {
            return false
        }

        logger.d("$displayName is near bobber")

        return isCaughtFishVelocity(playerPos)
    }

    private fun Entity.isCaughtFishVelocity(playerPos: Vec3d): Boolean {
        // FishingBobberEntity.use(ItemStack usedItem):
        // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
        // double d = playerEntity.x - x;
        // double e = playerEntity.y - y;
        // double f = playerEntity.z - z;
        // double g = 0.1;
        // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

        val relativeX = playerPos.x - pos.x
        if ((relativeX * G - velocity.x).absoluteValue > ERROR) return false

        val relativeZ = playerPos.z - pos.z
        if ((relativeZ * G - velocity.z).absoluteValue > ERROR) return false

        val relativeY = playerPos.y - pos.y
        val d = relativeX * relativeX + relativeY * relativeY + relativeZ * relativeZ
        if ((relativeY * G + sqrt(sqrt(d)) * 0.08 - velocity.y).absoluteValue > ERROR) {
            return false
        }

        logger.d("caught item $displayName matches caught item velocity")

        return true
    }

    companion object {
        private const val ERROR = 0.01
        private const val G = 0.1

        private fun Entity.nearBobber(
            bobberPos: Vec3d,
            bobberPrePos: Vec3d,
            bobberVel: Vec3d,
            getComponent: (Vec3d) -> Double,
        ): Boolean {
            val c = getComponent(bobberPos)
            return absLess(
                getComponent(pos) - c,
                c - getComponent(bobberPrePos),
                getComponent(bobberVel),
            )
        }

        private fun absLess(a: Double, vararg b: Double): Boolean {
            val abs = a.absoluteValue
            return b.any { abs < it.absoluteValue + ERROR }
        }
    }
}
