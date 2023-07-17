package org.c0nstexpr.fishology.core.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand

class UseRodEvent private constructor() {
    data class Arg(
        val hand: Hand,
        val player: PlayerEntity
    )

    companion object {
        @JvmField
        internal val afterUseMutableFlow = MutableStateFlow<Arg?>(null)

        val afterUseFlow = afterUseMutableFlow.asStateFlow()

        @JvmField
        internal val beforeUseMutableFlow = MutableStateFlow<Arg?>(null)

        val beforeUseFlow = beforeUseMutableFlow.asStateFlow()
    }
}
