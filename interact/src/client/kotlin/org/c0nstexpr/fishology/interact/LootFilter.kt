package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.concatMapMaybe
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.flatMapMaybe
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.MOD_ID
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.ItemEntitySpawnEvent
import org.c0nstexpr.fishology.events.ItemEntityTrackerEvent
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame
import org.c0nstexpr.fishology.utils.trackedPos
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.absoluteValue

class LootFilter(private val rod: Rod, private val caught: Observable<ItemEntity>) :
    SwitchDisposable() {
    var lootSet = setOf<FishingLoot>()
        set(value) {
            field = value
            logger.d<LootFilter> { "Change discard loots" }
        }

    private val lootSubject = PublishSubject<Loot>()

    var judgeThreshold: Double = 0.1
        set(value) {
            field = value
            logger.d<LootFilter> { "Change loot judge threshold to $value" }
        }

    val loot: Observable<Loot> = lootSubject

    override fun onEnable(): Disposable {
        logger.d<LootFilter> { "enable throw loot interaction" }

        var notified = false

        val notify = {
            if (!notified) {
                rod.client.msg(Text.translatable("$MOD_ID.discard_loots_notification"))
                notified = true
            }
        }

        return caught.switchMapMaybe switch@{ entity ->
            val stack = entity.stack
            val rodItem = rod.rodItem ?: return@switch maybeOfEmpty()
            val player = rodItem.player

            if (rodItem.slotIndex == player.inventory.selectedSlot) {
                logger.d<LootFilter> { "rod is selected, aborting" }
                notify()
                lootSubject.onNext(Loot(entity, false))
                return@switch maybeOfEmpty()
            }

            if (!lootSet.contains(stack.getLoot())) {
                lootSubject.onNext(Loot(entity, false))
                return@switch maybeOfEmpty()
            }

            val copied = stack.copy()

            SlotUpdateEvent.observable.filter {
                it.syncId == player.playerScreenHandler.syncId && it.stack.isSame(copied)
            }
                .map { Pair(player.playerScreenHandler.getSlot(it.slot).index, copied) }
                .firstOrComplete()
        }
            .concatMapMaybe concat@{ (slot, stack) ->
                val manager = rod.client.interactionManager
                val player = rod.player

                if (player == null) {
                    logger.w<LootFilter> { "client player is null" }
                    lootSubject.onNext(Loot(null, false))
                    return@concat maybeOfEmpty()
                }

                if (manager == null) {
                    logger.w<LootFilter> { "interaction manager is null" }
                    lootSubject.onNext(Loot(null, false))
                    return@concat maybeOfEmpty()
                }

                manager.pickFromInventory(slot)

                SelectedSlotUpdateEvent.observable.filter {
                    stack.isSame(player.inventory.getStack(it.slot))
                }
                    .map { Pair(player, stack) }
                    .firstOrComplete()
            }
            .flatMapMaybe { (p, stack) ->
                if (p.dropSelectedItem(false)) {
                    logger.d<LootFilter> { "dropped excluded loot" }
                    p.swingHand(Hand.MAIN_HAND)

                    ItemEntitySpawnEvent.observable.switchMap {
                        ItemEntityTrackerEvent.observable.map { it.entity }
                            .filter { it.isDropped(p, stack, judgeThreshold) }
                    }
                        .firstOrComplete()
                } else {
                    logger.w<LootFilter> { "failed to drop discard loot" }
                    maybeOfEmpty()
                }
            }
            .subscribe {
                lootSubject.onNext(Loot(it, true))
            }
    }

    companion object {
        private fun ItemEntity.isDropped(
            player: PlayerEntity,
            stack: ItemStack,
            judgeThreshold: Double
        ): Boolean {
            if (!this.stack.isSame(stack)) return false

            fun isErrorUnaccepted(error: Double) = if (error > judgeThreshold) {
                logger.d<CaughtFish> {
                    "drop item candidate out of threshold, error: $error, threshold: $judgeThreshold"
                }
                true
            } else false

            // double d = this.getEyeY() - 0.3F;
            // ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), d, this.getZ(), stack);

            val pos = pos
            val playerPos = player.trackedPos.run { Vec3d(x, eyeY - 0.3, z) }

            if (vecComponents.any { isErrorUnaccepted((it(pos) - it(playerPos)).absoluteValue) })
                return false

            // float f = 0.3F;
            // float g = MathHelper.sin(pitch * Math.PI / 180.0);
            // float h = MathHelper.cos(pitch * Math.PI / 180.0);
            // float i = MathHelper.sin(yaw * Math.PI / 180.0);
            // float j = MathHelper.cos(yaw * Math.PI / 180.0);
            // float k = this.random.nextFloat() *  (Math.PI * 2);
            // float l = 0.02F * this.random.nextFloat();
            // itemEntity.setVelocity(
            //     (-i * h * f) + Math.cos(k) * l, -- 0.02
            //     (-g * f + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F), -- 0.1
            //     (j * h * f) + Math.sin(k) * l -- 0.02
            // );

            val vel = velocity

            val f = 0.3
            val p = Math.PI / 180
            val pitchPi = (pitch * p).toFloat()
            val yawPi = (yaw * p).toFloat()
            val h = MathHelper.cos(pitchPi)

            val targetVel =
                Vec3d(
                    -MathHelper.sin(yawPi) * h * f,
                    -MathHelper.sin(pitchPi) * f + 0.1,
                    MathHelper.cos(yawPi) * h * f
                )

            val errorVec = Vec3d(0.02, 0.1, 0.02)

            return vecComponents.all {
                !isErrorUnaccepted((it(vel) - it(targetVel)).absoluteValue - it(errorVec))
            }
        }
    }
}
