package org.c0nstexpr.fishology.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.projectile.FishingBobberEntity

fun interface BobberStateChangeEvents {
    fun change(bobber: FishingBobberEntity, state: FishingBobberEntity.State)

    companion object {
        @JvmField
        val EVENT: Event<BobberStateChangeEvents> =
            EventFactory.createArrayBacked(BobberStateChangeEvents::class.java) { listeners ->
                BobberStateChangeEvents { bobber, state ->
                    for (listener in listeners)
                        listener.change(bobber, state)
                }
            }
    }
}
