package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.utils.initObserve
import org.c0nstexpr.fishology.core.config.FishologyConfig
import org.c0nstexpr.fishology.core.config.FishologyConfigModel
import org.c0nstexpr.fishology.interact.AutoFishingInteraction
import org.c0nstexpr.fishology.interact.RodInteraction

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val rodInteraction = RodInteraction(client)

    var fishingInteraction: AutoFishingInteraction? = null
        private set

    val config: FishologyConfig = FishologyConfig.createAndLoad()

    var enabled
        get() = config.enabled()
        set(value) = config.enabled(value)

    init {
        logger.debug("Initializing main controller")

        config.initObserve(FishologyConfigModel::enabled) {
            if (!it) {
                logger.debug("Disabling auto fishing")

                fishingInteraction?.dispose()
                fishingInteraction = null

                return@initObserve
            }

            logger.debug("Enabling auto fishing")

            if (fishingInteraction == null)
                fishingInteraction = AutoFishingInteraction(RodInteraction(client))
        }

        doOnDispose {
            enabled = false
            rodInteraction.dispose()
        }
    }
}
