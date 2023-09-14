package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.notNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.ItemEntitySpawnEvent
import org.c0nstexpr.fishology.events.ItemEntityTrackerEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.observableStep
import org.c0nstexpr.fishology.utils.trackedPos
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class CaughtFish(private val rod: Rod) : SwitchDisposable() {
    private val caughtSubject = PublishSubject<ItemEntity?>()

    val caught: Observable<ItemEntity?> = caughtSubject

    var judgeThreshold: Double = 0.1
        set(value) {
            field = value
            logger.d("Change caught judge threshold to $value")
        }

    private val rodItemObservable get() = rod.itemObservable.notNull()

    override fun onEnable(): Disposable {
        logger.d("enable caught fish interaction")

        val rodCast = onRodCast()

        return rodItemObservable.filter { it.inUse }
            .switchMapMaybe { rodCast }
            .tryOn()
            .subscribe {
                it.run { logger.d("caught item: ${stack.item.name.string}, pos: $pos, tracked pos: $trackedPos") }
                caughtSubject.onNext(it)
            }
    }

    private fun onRodCast() =
        CaughtFishEvent.observable.filter { it.caught && it.bobber.id == rod.player?.fishHook?.id }
            .firstOrComplete()
            .map { it.bobber }
            .flatMap(::onCaughtFish)

    private fun onCaughtFish(bobber: FishingBobberEntity): Maybe<ItemEntity> {
        logger.d("caught fish")
        caughtSubject.onNext(null)

        return rodItemObservable.filter { !it.inUse }
            .firstOrComplete()
            .flatMap { onRodRetrieve(bobber.owner?.trackedPos ?: return@flatMap maybeOfEmpty()) }
    }

    private fun onRodRetrieve(pPos: Vec3d) =
        observableStep(ItemEntitySpawnEvent.observable)
            .concatMaybe(
                {
                    ItemEntityTrackerEvent.observable.filter { isCaughtItem(pPos, judgeThreshold) }
                        .firstOrComplete()
                },
            )
            .firstOrComplete()
            .map { it.entity }

    companion object {
        fun ItemEntitySpawnEvent.Arg.isCaughtItem(pPos: Vec3d, errorThreshold: Double): Boolean {
            // FishingBobberEntity.use(ItemStack usedItem):
            // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
            // double d = playerEntity.x - x;
            // double e = playerEntity.y - y;
            // double f = playerEntity.z - z;
            // double g = 0.1;
            // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

            fun isErrorAccepted(error: Double) = if (error > errorThreshold) {
                logger.d("caught item candidate out of threshold, error: $error, threshold: $errorThreshold")
                true
            } else {
                false
            }

            var relative = Vec3d(pPos.x - pos.x, 0.0, 0.0)
            var errorVec = Vec3d((relative.x * G - vel.x).absoluteValue, 0.0, 0.0)
            if (isErrorAccepted(errorVec.x)) return false

            relative = Vec3d(relative.x, 0.0, pPos.z - pos.z)
            errorVec = Vec3d(errorVec.x, (relative.z * G - vel.z).absoluteValue, 0.0)
            if (isErrorAccepted(errorVec.y)) return false

            relative = Vec3d(relative.x, pPos.y - pos.y, relative.z)
            errorVec = Vec3d(
                errorVec.x,
                errorVec.y,
                (relative.y * G + sqrt(relative.length()) * 0.08 - vel.y).absoluteValue,
            )
            if (isErrorAccepted(errorVec.y)) return false

            logger.d("caught item candidate accepted, error vec: $errorVec, threshold: $errorThreshold")

            return true
        }

        private const val G = 0.1
    }
}
