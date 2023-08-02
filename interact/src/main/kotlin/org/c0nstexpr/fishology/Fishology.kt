package org.c0nstexpr.fishology

import co.touchlab.kermit.Severity
import com.badoo.reaktive.disposable.scope.DisposableScope
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.c0nstexpr.fishology.core.config.ConfigControl
import org.c0nstexpr.fishology.core.config.FishologyCoreConfig
import org.c0nstexpr.fishology.core.config.FishologyCoreConfigModel
import org.c0nstexpr.fishology.interact.AutoFishingInteraction
import org.c0nstexpr.fishology.interact.BobberInteraction
import org.c0nstexpr.fishology.interact.RodInteraction
import org.c0nstexpr.fishology.utils.Module
import org.c0nstexpr.fishology.utils.initObserve

class Fishology(
    val client: MinecraftClient,
    var handler: ClientPlayNetworkHandler,
) : DisposableScope by DisposableScope() {
    val config: FishologyCoreConfig by ConfigControl.config

    private var rod = object : Module() {
        var value: RodInteraction? = null
            private set(value) {
                field?.dispose()
                field = value
            }

        override fun onCreate() {
            value = RodInteraction(client)
        }

        override fun onDestroy() {
            value = null
        }
    }

    private var bobber = object : Module() {
        var value: BobberInteraction? = null
            private set(value) {
                field?.dispose()
                value?.enableChat = config.enableChatOnCaught()
                field = value
            }

        override fun onCreate() {
            value = BobberInteraction(client)
        }

        override fun onDestroy() {
            value = null
        }
    }

    private var fishing = object : Module() {
        var value: AutoFishingInteraction? = null
            private set(value) {
                field?.dispose()
                field = value
            }

        override fun onCreate() {
            val rod = this@Fishology.rod
            val bobber = this@Fishology.bobber

            add(rod)
            add(bobber)

            value =
                AutoFishingInteraction(rod.value!!::use, handler.profile.id, bobber.value!!.hook)
        }

        override fun onDestroy() {
            value = null
            super.onDestroy()
        }
    }

    private val configModule = object : Module() {
        val config by ConfigControl.config

        override fun onCreate() = Unit

        init {
            config.initObserve(FishologyCoreConfigModel::enableAutoFish) { onEnableAutoFish(it) }
            config.initObserve(FishologyCoreConfigModel::enableChatOnCaught) {
                onEnableChatOnCaught(it)
            }
            config.initObserve(FishologyCoreConfigModel::logLevel) { onChangeLogLevel(it) }
        }

        private fun onChangeLogLevel(it: Severity) {
            logger.mutableConfig.minSeverity = it
        }

        private fun onEnableAutoFish(it: Boolean) {
            if (!it) {
                logger.d("Disable auto fishing")
                remove(fishing)
                return
            }

            logger.d("Enable auto fishing")
            add(fishing)
        }

        private fun onEnableChatOnCaught(it: Boolean) {
            logger.d("${if (it) "Enable" else "Disable"} chat on caught")

            if (!it) {
                remove(bobber)
                bobber.value?.enableChat = false

                return
            }

            add(bobber)
        }
    }

    init {
        logger.d("Initializing main module")
    }
}
