package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.BobberOwnedEvent
import org.c0nstexpr.fishology.events.ItemEntityVelPacketEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class CaughtFish(private val id: UUID) : SwitchDisposable() {
    var bobber = null as FishingBobberEntity?
        private set

    private val caughtSubject = PublishSubject<ItemEntity>()

    val caught: Observable<ItemEntity> = caughtSubject

    var caughtItemId: UUID = UUID.randomUUID()
        private set

    override fun onEnable() = disposableScope {
        BobberOwnedEvent.observable.filter { id == it.player.uuid }
            .subscribeScoped { bobber = it.bobber }

        ItemEntityVelPacketEvent.observable.map { it.entity }
            .filter { it.isCaughtItem() }
            .subscribeScoped {
                logger.d("Caught item: ${it.displayName}")
                caughtSubject.onNext(it)
                caughtItemId = it.uuid
            }
    }

    private fun Entity.isCaughtItem(): Boolean {
        if (caughtItemId == uuid) return false

        logger.d("check caught item is near bobber")

        val bobber = bobber ?: return false
        if (
            nearBobber(bobber, Vec3d::x, Entity::prevX) &&
            nearBobber(bobber, Vec3d::y, Entity::prevY) &&
            nearBobber(bobber, Vec3d::z, Entity::prevZ)
        ) {
            return false
        }

        return isHookedVelocity(bobber)
    }

    private fun Entity.isHookedVelocity(bobber: FishingBobberEntity): Boolean {
        // bobber use code:
        // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
        // double d = playerEntity.x - x;
        // double e = playerEntity.y - y;
        // double f = playerEntity.z - z;
        // double g = 0.1;
        // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
        logger.d("check caught item matches hooked velocity")

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

        return true
    }

    companion object {
        private const val ERROR = 0.01
        private const val G = 0.1

        private fun Entity.nearBobber(
            bobber: FishingBobberEntity,
            getComponent: (Vec3d) -> Double,
            getPre: (Entity) -> Double,
        ): Boolean {
            val c = getComponent(bobber.pos)
            return absLess(getComponent(pos) - c, c - getPre(bobber), getComponent(bobber.velocity))
        }

        private fun absLess(a: Double, vararg b: Double) =
            b.all { a.absoluteValue < it.absoluteValue + 0.01 }
    }
}
