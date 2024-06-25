package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.concatMapMaybe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.fishHookRemovedObservable
import org.c0nstexpr.fishology.utils.spawnedItemMaybe
import org.c0nstexpr.fishology.utils.trackedPos
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class CaughtFish : SwitchDisposable() {
    private val caughtSubject = PublishSubject<ItemEntity>()

    val caught: Observable<ItemEntity> = caughtSubject

    var judgeThreshold: Double = 0.1
        set(value) {
            field = value
            logger.d<CaughtFish> { "Change caught judge threshold to $value" }
        }

    override fun onEnable(): Disposable {
        logger.d<CaughtFish> { "enable caught fish interaction" }

        return CaughtFishEvent.observable.filter { it.caught }
            .switchMapMaybe {
                merge(
                    UseRodEvent.observable.filter { !it.isThrow }.map { it.player.trackedPos },
                    fishHookRemovedObservable().map { null }
                ).firstOrComplete()
            }
            .notNull()
            .concatMapMaybe { pos -> spawnedItemMaybe { it.isCaughtItem(pos) } }
            .subscribe {
                it.run {
                    logger.d<CaughtFish> {
                        "caught item: ${stack.item.name.string}, pos: $pos, tracked pos: $trackedPos"
                    }
                }
                caughtSubject.onNext(it)
            }
    }

    private fun ItemEntity.isCaughtItem(pPos: Vec3d): Boolean {
        // FishingBobberEntity.use(ItemStack usedItem):
        // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
        // double d = playerEntity.x - x;
        // double e = playerEntity.y - y;
        // double f = playerEntity.z - z;
        // double g = 0.1;
        // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

        fun isErrorUnaccepted(error: Double) = if (error > judgeThreshold) {
            logger.d<CaughtFish> {
                "caught item candidate out of threshold, error: $error, threshold: $judgeThreshold"
            }
            true
        } else {
            false
        }

        var relative = Vec3d(pPos.x - pos.x, 0.0, 0.0)
        var errorVec = Vec3d((relative.x * G - velocity.x).absoluteValue, 0.0, 0.0)
        if (isErrorUnaccepted(errorVec.x)) return false

        relative = Vec3d(relative.x, 0.0, pPos.z - pos.z)
        errorVec = Vec3d(errorVec.x, (relative.z * G - velocity.z).absoluteValue, 0.0)
        if (isErrorUnaccepted(errorVec.y)) return false

        relative = Vec3d(relative.x, pPos.y - pos.y, relative.z)
        errorVec = Vec3d(
            errorVec.x,
            errorVec.y,
            (relative.y * G + sqrt(relative.length()) * 0.08 - velocity.y).absoluteValue
        )
        if (isErrorUnaccepted(errorVec.y)) return false

        logger.d<CaughtFish> {
            "caught item candidate accepted, error vec: $errorVec, threshold: $judgeThreshold"
        }

        return true
    }

    companion object {
        private const val G = 0.1
    }
}
