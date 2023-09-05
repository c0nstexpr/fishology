package org.c0nstexpr.fishology.interact

import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.ItemEntitySpawnEvent
import org.c0nstexpr.fishology.logger
import kotlin.math.absoluteValue
import kotlin.math.sqrt

fun ItemEntitySpawnEvent.Arg.isCaughtItem(pPos: Vec3d, errorThreshold: Double): Boolean {
    // FishingBobberEntity.use(ItemStack usedItem):
    // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
    // double d = playerEntity.x - x;
    // double e = playerEntity.y - y;
    // double f = playerEntity.z - z;
    // double g = 0.1;
    // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

    fun validateError(error: Double) = if (error > errorThreshold) {
        logger.d("caught item candidate out of error: $error")
        true
    } else {
        false
    }

    val relativeX = pPos.x - pos.x
    if (validateError((relativeX * G - vel.x).absoluteValue)) return false

    val relativeZ = pPos.z - pos.z
    if (validateError((relativeZ * G - vel.z).absoluteValue)) return false

    val relativeY = pPos.y - pos.y
    val d = relativeX * relativeX + relativeY * relativeY + relativeZ * relativeZ
    if (validateError((relativeY * G + sqrt(sqrt(d)) * 0.08 - vel.y).absoluteValue)) return false

    return true
}

private const val G = 0.1
