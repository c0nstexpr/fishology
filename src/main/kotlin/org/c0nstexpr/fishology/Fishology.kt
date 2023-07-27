package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.c0nstexpr.fishology.core.config.FishologyConfig
import org.c0nstexpr.fishology.core.config.FishologyConfigModel
import org.c0nstexpr.fishology.core.log.*
import org.c0nstexpr.fishology.interact.AutoFishingInteraction
import org.c0nstexpr.fishology.interact.RodInteraction
import org.c0nstexpr.fishology.utils.initObserve

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    val rodInteraction = RodInteraction(client)
    val fishingInteraction = AutoFishingInteraction(client.player,
        { rodInteraction.use() })

    val config: FishologyConfig = FishologyConfig.createAndLoad()

    var enabled
        get() = config.enabled()
        set(value) = config.enabled(value)

    init {
        logger.d("Initializing main controller")

        config.initObserve(FishologyConfigModel::enabled) {
            if (!it) {
                logger.d("Disabling auto fishing")

                return@initObserve
            }

            logger.d("Enabling auto fishing")

            if (fishingInteraction == null) {
                fishingInteraction = AutoFishingInteraction(RodInteraction(client))
            }
        }

        doOnDispose {
            enabled = false
            rodInteraction.dispose()
        }
    }
}
