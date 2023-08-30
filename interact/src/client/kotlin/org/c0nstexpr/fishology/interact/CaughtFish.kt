package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntityVelPacketEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class CaughtFish(private val rod: Rod) : SwitchDisposable() {
    private val caughtSubject = PublishSubject<ItemEntity?>()

    val caught: Observable<ItemEntity?> = caughtSubject

    var caughtItemId: UUID = UUID.randomUUID()
        private set

    override fun onEnable(): Disposable {
        logger.d("enable caught fish interaction")

        return CaughtFishEvent.observable.filter {
            it.caught && (it.bobber.playerOwner?.id ?: return@filter false) == rod.player?.id
        }
            .switchMapMaybe {
                caughtSubject.onNext(null)

                ItemEntityVelPacketEvent.observable.map { it.entity }
                    .filter { it.isCaughtItem() }
                    .firstOrComplete()
            }
            .subscribe {
                logger.d("caught fish: ${it.displayName}")
                caughtSubject.onNext(it)
                caughtItemId = it.uuid
            }
    }

    private fun Entity.isCaughtItem(): Boolean {
        if (caughtItemId == uuid) return false

        val bobber = rod.bobber ?: return false
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
        // FishingBobberEntity.use(ItemStack usedItem):
        // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
        // double d = playerEntity.x - x;
        // double e = playerEntity.y - y;
        // double f = playerEntity.z - z;
        // double g = 0.1;
        // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

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

        logger.d("caught item $this is near bobber")

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
