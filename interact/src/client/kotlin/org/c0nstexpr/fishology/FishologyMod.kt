package org.c0nstexpr.fishology

import com.github.shynixn.mccoroutine.fabric.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.c0nstexpr.fishology.config.Config
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.config.ConfigModel
import org.c0nstexpr.fishology.log.getLogger
import org.c0nstexpr.fishology.log.spell
import org.c0nstexpr.fishology.utils.observe
import java.nio.file.Path

const val MOD_ID = "fishology"
const val MOD_NAME = "Fishology"

internal val logger = getLogger(MOD_NAME)

val config: Config by ConfigControl.config

var fishology: Fishology? = null
    private set

val dataDir: Path get() = FabricLoader.getInstance().gameDir.resolve(MOD_ID)

internal object FishologyMod : ClientModInitializer {
    override fun onInitializeClient() {
        config.observe(ConfigModel::logLevel) {
            CoreLogger.mutableConfig.minSeverity = it
            logger.mutableConfig.minSeverity = it

            logger.d("set log level to $it")
        }

        ClientLifecycleEvents.CLIENT_STARTED.register {
            logger.i(spell())

            mcCoroutineConfiguration.minecraftExecutor = it
        }

        MCCoroutineExceptionEvent.EVENT.register(
            object : MCCoroutineExceptionEvent {
                override fun onMCCoroutineException(
                    throwable: Throwable,
                    entryPoint: Any
                ): Boolean {
                    logger.e(throwable) { "MC Coroutine exception at $entryPoint" }
                    return true
                }
            }
        )

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if (fishology?.isDisposed != false) fishology = Fishology(client)
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            fishology?.dispose()
            fishology = null
        }
    }
}
