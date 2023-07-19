@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.config.initObserve
import org.c0nstexpr.fishology.core.config.FishologyConfig
import org.c0nstexpr.fishology.core.config.FishologyConfigModel
import org.c0nstexpr.fishology.interact.FishingInteraction
import org.c0nstexpr.fishology.interact.RodInteraction

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val rodInteraction = RodInteraction(client)

    var fishingInteraction: FishingInteraction? = null
        private set

    val config: FishologyConfig = FishologyConfig.createAndLoad()

    var enabled
        get() = config.enabled()
        set(value) = config.enabled(value)

    init {
        config.initObserve(FishologyConfigModel::enabled) {
            if (!it) {
                fishingInteraction?.dispose()
                fishingInteraction = null

                return@initObserve
            }

            if (fishingInteraction == null) {
                fishingInteraction = FishingInteraction(RodInteraction(client))
            }
        }

        doOnDispose {
            enabled = false
            rodInteraction.dispose()
        }
    }
}
