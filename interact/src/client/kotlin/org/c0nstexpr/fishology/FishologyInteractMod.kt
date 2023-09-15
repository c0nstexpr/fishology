package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.c0nstexpr.fishology.log.LogBuilder
import org.c0nstexpr.fishology.log.greeting
import java.nio.file.Path

const val modId = "fishology"
const val modName = "Fishology"

internal val logger = LogBuilder().apply { tag = modId }.build()

var fishology: Fishology? = null
    private set

val dataDir: Path get() = FabricLoader.getInstance().gameDir.resolve(modId)

internal fun init() {
    logger.greeting()

    ClientPlayConnectionEvents.JOIN.register { _, _, client ->
        if (fishology?.isDisposed != false) fishology = Fishology(client)

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            fishology?.dispose()
            fishology = null
        }
    }
}
