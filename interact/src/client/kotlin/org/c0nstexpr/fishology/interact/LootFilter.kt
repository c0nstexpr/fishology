package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.flatMap
import com.badoo.reaktive.maybe.map
import com.badoo.reaktive.maybe.maybeOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMapMaybe
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.MOD_ID
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SelectedSlotUpdateEvent
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.log.w
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame
import org.c0nstexpr.fishology.utils.spawnedItemMaybe
import org.c0nstexpr.fishology.utils.trackedPos
import org.c0nstexpr.fishology.utils.vecComponents
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

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

    var loot: Observable<Loot> = caught.map { Loot(it, false) }
        private set

    private var notified = false

    override fun onDisable() {
        super.onDisable()
        loot = caught.map { Loot(it, false) }
    }

    override fun onEnable(): Disposable {
        logger.d<LootFilter> { "enable throw loot interaction" }
        notified = false
        loot = lootSubject
        return caught.switchMapMaybe(::caughtMaybe).subscribe { lootSubject.onNext(Loot(it, true)) }
    }

    private fun caughtMaybe(entity: ItemEntity): Maybe<ItemEntity> {
        val stack = entity.stack

        logger.d<LootFilter> { "caught loot: ${stack.name}" }

        if (!lootSet.contains(stack.getLoot())) {
            lootSubject.onNext(Loot(entity, false))
            return maybeOfEmpty()
        }

        logger.d<LootFilter> { "caught loot need discard" }

        val player = rod.rodItem?.run {
            if (slotIndex == player.inventory.selectedSlot) {
                logger.d<LootFilter> { "rod is selected, aborting" }
                if (!notified) {
                    rod.client.msg(Text.translatable("$MOD_ID.discard_loots_notification"))
                    notified = true
                }

                lootSubject.onNext(Loot(entity, false))
                return@run null
            }

            player
        } ?: return maybeOfEmpty()

        val copied = stack.copy()
        val screenHandler = player.playerScreenHandler

        return SlotUpdateEvent.observable
            .filter { it.syncId == screenHandler.syncId && it.stack.isSame(copied) }
            .firstOrComplete()
            .map { screenHandler.getSlot(it.slot).index }
            .flatMap { dropLootMaybe(it, copied, player) }
    }

    private fun dropLootMaybe(
        slot: Int,
        stack: ItemStack,
        player: ClientPlayerEntity
    ): Maybe<ItemEntity> {
        val manager = rod.client.interactionManager

        if (manager == null) {
            logger.w<LootFilter> { "interaction manager is null" }
            lootSubject.onNext(Loot(null, false))
            return maybeOfEmpty()
        }

        logger.d<LootFilter> { "pick excluded loot" }

        manager.pickFromInventory(slot)

        return SelectedSlotUpdateEvent.observable.filter {
            stack.isSame(player.inventory.getStack(it.slot))
        }
            .firstOrComplete()
            .flatMap {
                if (player.dropSelectedItem(false)) {
                    logger.d<LootFilter> { "dropped excluded loot" }
                    player.swingHand(Hand.MAIN_HAND)

                    val playerPos = player.run { trackedPos.run { Vec3d(x, eyeY - 0.3, z) } }
                    val playerPitch = player.pitch
                    val playerYaw = player.yaw
                    spawnedItemMaybe { it.isDropped(playerPos, playerPitch, playerYaw, stack) }
                } else {
                    logger.w<LootFilter> { "failed to drop discard loot" }
                    maybeOfEmpty()
                }
            }
    }

    private fun ItemEntity.isDropped(
        playerPos: Vec3d,
        pitch: Float,
        yaw: Float,
        stack: ItemStack
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

        val posErrorVec = pos.subtract(playerPos)

        if (vecComponents.any { isErrorUnaccepted(it(posErrorVec).absoluteValue) }) {
            logger.d<CaughtFish> { "pos error vec: $posErrorVec" }
            logger.d<CaughtFish> { "threshold: $judgeThreshold" }
            return false
        }

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

        val f = 0.3
        var dPitch = pitch.toDouble()
        var dYaw = yaw.toDouble()

        with(Math.PI / 180) {
            dPitch *= this
            dYaw *= this
        }

        val h = cos(dPitch)

        val targetVel = Vec3d(-sin(dYaw) * h * f, -sin(dPitch) * f + 0.1, cos(dYaw) * h * f)

        val velErrorVec = velocity.subtract(targetVel)

        if (
            vecComponents.any {
                isErrorUnaccepted(it(velErrorVec).absoluteValue - it(velThreshold))
            }
        ) {
            logger.d<CaughtFish> { "vel error vec: $velErrorVec    vel threshold: $velThreshold" }
            logger.d<CaughtFish> { "threshold: $judgeThreshold" }
            return false
        }

        logger.d<CaughtFish> { "drop item candidate accepted with" }
        logger.d<CaughtFish> { "pos error vec: $posErrorVec" }
        logger.d<CaughtFish> { "vel error vec: $velErrorVec    vel threshold: $velThreshold" }
        logger.d<CaughtFish> { "threshold: $judgeThreshold" }

        return true
    }

    companion object {
        private val velThreshold = Vec3d(0.02, 0.1, 0.02)
    }
}
