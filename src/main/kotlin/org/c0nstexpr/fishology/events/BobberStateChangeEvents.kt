package org.c0nstexpr.fishology.events

import io.ktor.events.*
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.projectile.FishingBobberEntity

fun interface BobberCaughtFishEvents {
    fun caught(bobber: FishingBobberEntity)

    companion object {
        @JvmField
        val Events = EventDefinition<BobberCaughtFishEvents>()
    }
}
