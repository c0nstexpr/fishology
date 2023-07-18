@file:Suppress("MemberVisibilityCanBePrivate")

package org.c0nstexpr.fishology

import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import org.c0nstexpr.fishology.config.initObserve
import org.c0nstexpr.fishology.core.config.FishologyConfig
import org.c0nstexpr.fishology.core.config.FishologyConfigModel
import org.c0nstexpr.fishology.interact.FishingInteraction
import org.c0nstexpr.fishology.interact.RodInteraction
import org.c0nstexpr.fishology.utils.coroutineScope

class Fishology(val client: MinecraftClient) : DisposableScope by DisposableScope() {
    var fishingInteraction: FishingInteraction? = null
        private set(value) {
            if (value == null) {
                interruptionJob?.cancel()
                interruptionJob = null
            } else interruptionJob = client.coroutineScope.launch {
                value.interruptionFlow.collect { fishingInteraction?.dispose() }
            }

            field = value
        }

    private var fishingJob: Job? = null
        set(value) {
            if (value == null) {
                fishingInteraction?.dispose()
                fishingInteraction = null
            }

            field = value
        }

    private var interruptionJob: Job? = null

    var rodInteraction: RodInteraction? = null
        private set(value) {
            if (value == null) {
                fishingJob?.cancel()
                fishingJob = null

                interruptionJob?.cancel()
                interruptionJob = null
            } else {
                fishingJob = client.coroutineScope.launch {
                    value.beforeUseFlow.filter {
                        (fishingInteraction == null) &&
                            (it != null) &&
                            (it.player.fishHook == null)
                    }
                        .collect { fishingInteraction = FishingInteraction(value) }
                }

                interruptionJob =
            }
            field = value
        }


    val config: FishologyConfig = FishologyConfig.createAndLoad()

    var enabled: Boolean
        get() = config.enabled()
        set(value) = config.enabled(value)

    init {
        config.initObserve(FishologyConfigModel::enabled) {
            if (!it) {
                fishingInteraction?.dispose()
                rodInteraction?.dispose()
                rodInteraction = null

                return@initObserve
            }

            rodInteraction = RodInteraction(client)
        }

        val

        doOnDispose { enabled = false }
    }
}
