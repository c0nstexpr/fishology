package org.c0nstexpr.fishology.core.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.minecraft.entity.projectile.FishingBobberEntity

class CaughtFishEvent private constructor() {
    data class Arg(val bobber: FishingBobberEntity, val caught: Boolean)

    companion object {
        @JvmField
        internal val mutableFlow = MutableSharedFlow<Arg>()

        val flow = mutableFlow.asSharedFlow()
    }
}
