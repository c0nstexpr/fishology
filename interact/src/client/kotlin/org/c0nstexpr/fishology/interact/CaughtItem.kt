package org.c0nstexpr.fishology.interact

import net.minecraft.util.math.Vec3d
import org.c0nstexpr.fishology.events.ItemEntitySpawnEvent
import org.c0nstexpr.fishology.logger
import kotlin.math.absoluteValue
import kotlin.math.sqrt

fun ItemEntitySpawnEvent.Arg.isCaughtItem(
    bPos: Vec3d,
    bVel: Vec3d,
    pPos: Vec3d,
    posError: Double,
) =
    if (nearBobber(bPos, bVel, posError)) {
        logger.d("${entity.stack.item.name.string} is near bobber")
        isCaughtFishVelocity(pPos)
    } else {
        false
    }

private fun ItemEntitySpawnEvent.Arg.nearBobber(
    bobberPos: Vec3d,
    bobberVel: Vec3d,
    posError: Double,
) =
    vecGetters.all { getter ->
        val b = bobberPos.getter()
        val abs = (pos.getter() - b).absoluteValue
        val error = abs - bobberVel.getter().absoluteValue

        logger.d("num error: $error")
        error < posError
    }

private fun ItemEntitySpawnEvent.Arg.isCaughtFishVelocity(pPos: Vec3d): Boolean {
    // FishingBobberEntity.use(ItemStack usedItem):
    // ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack2);
    // double d = playerEntity.x - x;
    // double e = playerEntity.y - y;
    // double f = playerEntity.z - z;
    // double g = 0.1;
    // itemEntity.setVelocity(d * 0.1, e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);

    val relativeX = pPos.x - pos.x
    if ((relativeX * G - vel.x).absoluteValue > ERROR) return false

    val relativeZ = pPos.z - pos.z
    if ((relativeZ * G - vel.z).absoluteValue > ERROR) return false

    val relativeY = pPos.y - pos.y
    val d = relativeX * relativeX + relativeY * relativeY + relativeZ * relativeZ
    if ((relativeY * G + sqrt(sqrt(d)) * 0.08 - vel.y).absoluteValue > ERROR) {
        return false
    }

    logger.d("caught item ${entity.stack.item.name.string} matches caught item velocity")

    return true
}

private const val ERROR = 0.01
private const val G = 0.1

private val vecGetters = listOf<Vec3d.() -> Double>(Vec3d::x, Vec3d::y, Vec3d::z)
