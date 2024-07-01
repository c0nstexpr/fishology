package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.notNull
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.CaughtFishEvent
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.fishHookRemovedObservable
import org.c0nstexpr.fishology.utils.spawnedItemMaybe
import org.c0nstexpr.fishology.utils.trackedPos
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class LootDetect : SwitchDisposable() {
    private val lootSubject = PublishSubject<ItemEntity>()

    val loot: Observable<ItemEntity> = lootSubject

    var judgeThreshold: Double = 0.1
        set(value) {
            field = value
            logger.d<LootDetect> { "Change loot judge threshold to $value" }
        }

    override fun onEnable(): Disposable {
        logger.d<LootDetect> { "enable fishing loot detect" }

        return CaughtFishEvent.observable.filter { it.caught }
            .switchMapMaybe {
                merge(
                    UseRodEvent.observable.filter { !it.isThrow }
                        .map {
                            it.player.run {
                                val hook = fishHook
                                if (hook == null) {
                                    logger.w<LootDetect> { "hook is null" }
                                    null
                                } else Pair(trackedPos, hook.trackedPos)
                            }
                        },
                    fishHookRemovedObservable().map { null }
                ).firstOrComplete()
                    .notNull()
                    .flatMap { (playerPos, bobberPos) ->
                        spawnedItemMaybe { it.isLoot(playerPos, bobberPos) }
                    }
            }
            .subscribe {
                logger.d<LootDetect> { "fish loot item: ${it.stack.item.name.string}" }
                lootSubject.onNext(it)
            }
    }

    private fun ItemEntity.isLoot(pPos: Vec3d, bobberPos: Vec3d): Boolean {
        fun isErrorUnaccepted(error: Double) = if (error > judgeThreshold) {
            logger.d<LootDetect> {
                "loot item candidate out of threshold, error: $error, threshold: $judgeThreshold"
            }
            true
        } else false

        // FishingBobberEntity.use(ItemStack usedItem):
        // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
        val posErrorVec = pos.subtract(bobberPos)

        if (
            vecComponents.any {
                isErrorUnaccepted(it(posErrorVec).absoluteValue - it(posThreshold))
            }
        ) {
            logger.d<LootDetect> { "pos error vec: $posErrorVec    pos threshold: $posThreshold" }
            logger.d<LootDetect> { "threshold: $judgeThreshold" }
            return false
        }

        // double d = playerEntity.x - x;
        // double e = playerEntity.y - y;
        // double f = playerEntity.z - z;
        // double g = 0.1;
        // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

        val targetVel = pPos.subtract(pos).let {
            it.multiply(G).run { Vec3d(x, y + sqrt(it.length()) * 0.08, z) }
        }

        val velErrorVec = velocity.subtract(targetVel)

        if (vecComponents.any { isErrorUnaccepted(it(velErrorVec).absoluteValue) }) {
            logger.d<LootDetect> { "vel error vec: $velErrorVec" }
            logger.d<LootDetect> { "threshold: $judgeThreshold" }
            return false
        }

        logger.d<LootDetect> { "loot item candidate accepted with" }
        logger.d<LootDetect> { "pos error vec: $posErrorVec    pos threshold: $posThreshold" }
        logger.d<LootDetect> { "vel error vec: $velErrorVec" }
        logger.d<LootDetect> { "threshold: $judgeThreshold" }

        return true
    }

    companion object {
        private const val G = 0.1

        private val posThreshold = Vec3d(0.0, 0.5, 0.0)
    }
}
