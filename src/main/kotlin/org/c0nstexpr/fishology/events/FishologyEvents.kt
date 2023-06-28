package org.c0nstexpr.fishology.events

import net.minecraft.entity.projectile.FishingBobberEntity

interface BobberStateChangeEvents {
    fun change(FishingBobberEntity.State ) -> Unit;

    companion object {
        @JvmField
        val EVENT: Event<BlockPlaceCallback> =
            EventFactory.createArrayBacked(BlockPlaceCallback::class.java) { listeners ->
                BlockPlaceCallback { world, pos, state, entity, source, player ->
                    for (listener in listeners) {
                        listener.place(world, pos, state, entity, source, player)
                    }
                }
            }
    }
}
