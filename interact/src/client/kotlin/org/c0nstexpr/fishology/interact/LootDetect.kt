package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
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
import org.c0nstexpr.fishology.events.EntityTrackerUpdateEvent
import org.c0nstexpr.fishology.events.ItemEntitySpawnEvent
import org.c0nstexpr.fishology.events.UseRodEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.fishHookRemovedObservable
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
            .switchMapMaybe { onCaught() }
            .subscribe {
                logger.d<LootDetect> { "fish loot item: ${it.stack.item.name.string}" }
                lootSubject.onNext(it)
            }
    }

    private fun onCaught() = merge(
        UseRodEvent.observable.filter { !it.isThrow }.map {
            it.player.run {
                val hook = fishHook
                if (hook == null) {
                    logger.w<LootDetect> { "hook is null" }
                    null
                } else Pair(trackedPos, hook.trackedPos)
            }
        },
        fishHookRemovedObservable().map { null }
    ).firstOrComplete().notNull().flatMap { (p, b) -> onHookedRetrieved(p, b) }

    private fun onHookedRetrieved(playerPos: Vec3d, bobberPos: Vec3d): Maybe<ItemEntity> {
        logger.d<LootDetect> { "error threshold: $judgeThreshold" }

        return ItemEntitySpawnEvent.observable.filter {
            val pos = it.pos

            isLootPos(pos, bobberPos) &&
                isLootVel(it.vel, playerPos.subtract(pos))
        }
            .firstOrComplete()
            .map { it.entity }
            .flatMap spawned@{ entity ->
                if (!entity.stack.isEmpty) return@spawned maybeOfEmpty()

                EntityTrackerUpdateEvent.observable.filter { it.id == entity.id }
                    .map { entity }
                    .firstOrComplete()
            }
    }

    private fun isLootPos(pos: Vec3d, bobberPos: Vec3d): Boolean {
        // FishingBobberEntity.use(ItemStack usedItem):
        // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
        val posErrorVec = pos.subtract(bobberPos)

        logger.d<LootDetect> { "pos error vec: $posErrorVec    pos threshold: $posThreshold" }

        if (vecComponents.any {
                it(posErrorVec).absoluteValue - it(posThreshold) > judgeThreshold
            }
        ) {
            logger.d<LootDetect> { "candidate pos not accepted" }
            return false
        }

        logger.d<LootDetect> { "candidate pos accepted" }
        return true
    }

    private fun isLootVel(vel: Vec3d, relPos: Vec3d): Boolean {
        // double d = playerEntity.x - x;
        // double e = playerEntity.y - y;
        // double f = playerEntity.z - z;
        // double g = 0.1;
        // itemEntity.setVelocity(d * g, e * g + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * g);

        val x = relPos.x * G
        val z = relPos.z * G

        logger.d<LootDetect> { "vel x: $relPos" }

        run {
            val xError = vel.x - x
            val zError = vel.z - z

            logger.d<LootDetect> { "vel x error: $xError    z error: $zError" }

            if (xError.absoluteValue > judgeThreshold || zError.absoluteValue > judgeThreshold) {
                logger.d<LootDetect> { "candidate velocity not accepted" }
                return false
            }
        }

        fun getY(y: Double) = y * G + sqrt(sqrt(x * x + y * y + z * z)) * 0.08

        val yRng = run {
            val y1 = getY(relPos.y - posThreshold.y - judgeThreshold)
            val y2 = getY(relPos.y + posThreshold.y + judgeThreshold)

            val minMax = if (y1 <= y2) y1 to y2 else y2 to y1

            minMax.first - judgeThreshold..minMax.second + judgeThreshold
        }
        val velY = vel.y

        logger.d<LootDetect> { "vel y rng: $yRng    candidate y: $velY" }

        if (velY !in yRng) {
            logger.d<LootDetect> { "candidate velocity not accepted" }
            return false
        }

        logger.d<LootDetect> { "candidate velocity accepted" }
        return true
    }

    companion object {
        private const val G = 0.1

        private val posThreshold = Vec3d(0.0, 1.0, 0.0)
    }
}
