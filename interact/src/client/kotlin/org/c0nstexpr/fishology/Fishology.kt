package org.c0nstexpr.fishology

import co.touchlab.kermit.Severity
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.disposable.scope.doOnDispose
import com.badoo.reaktive.observable.notNull
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.c0nstexpr.fishology.config.Config
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.config.ConfigModel
import org.c0nstexpr.fishology.interact.AutoFishingInteraction
import org.c0nstexpr.fishology.interact.BobberInteraction
import org.c0nstexpr.fishology.interact.RodInteraction
import org.c0nstexpr.fishology.log.MCMessageWriter
import org.c0nstexpr.fishology.log.addMCWriter
import org.c0nstexpr.fishology.log.removeWriterWhere
import org.c0nstexpr.fishology.utils.Module
import org.c0nstexpr.fishology.utils.initObserve

class Fishology(
    val client: MinecraftClient,
    var handler: ClientPlayNetworkHandler,
) : DisposableScope by DisposableScope() {
    val config: Config by ConfigControl.config

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

            add(rod, bobber)

            value =
                AutoFishingInteraction(
                    rod.value!!::use,
                    handler.profile.id,
                    bobber.value!!.caught.notNull(),
                )
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
            logger.mutableConfig.addMCWriter(client)
            doOnDispose { logger.mutableConfig.removeWriterWhere { w -> w is MCMessageWriter } }

            config.initObserve(ConfigModel::logLevel) { onChangeLogLevel(it) }
            config.initObserve(ConfigModel::enableAutoFish) { onEnableAutoFish(it) }
            config.initObserve(ConfigModel::enableChatOnCaught) { onEnableChatOnCaught(it) }
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
            bobber.value!!.enableChat = true
        }
    }

    init {
        logger.d("Initializing main module")
    }
}
