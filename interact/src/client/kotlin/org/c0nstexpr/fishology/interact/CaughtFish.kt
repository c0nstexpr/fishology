package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.concatMapMaybe
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
import org.c0nstexpr.fishology.utils.trackedPos

class CaughtFish(private val rod: Rod) : SwitchDisposable() {
    private val caughtSubject = PublishSubject<ItemEntity?>()

    val caught: Observable<ItemEntity?> = caughtSubject

    var posError: Double = 0.1

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
            .flatMap {
                onRodRetrieve(
                    bobber.trackedPos,
                    bobber.velocity,
                    bobber.owner?.trackedPos ?: return@flatMap maybeOfEmpty(),
                )
            }
    }

    private fun onRodRetrieve(bPos: Vec3d, bVel: Vec3d, pPos: Vec3d) =
        ItemEntitySpawnEvent.observable.concatMapMaybe { caught ->
            ItemEntityTrackerEvent.observable.filter {
                caught.isCaughtItem(bPos, bVel, pPos, posError)
            }
                .firstOrComplete()
        }
            .firstOrComplete()
            .map { it.entity }
}
