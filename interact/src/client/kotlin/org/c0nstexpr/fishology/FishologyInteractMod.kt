package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.c0nstexpr.fishology.log.LogBuilder
import org.c0nstexpr.fishology.log.greeting
import java.nio.file.Path

const val MOD_ID = "fishology"
const val MOD_NAME = "Fishology"

internal val logger = LogBuilder().apply { tag = MOD_ID }.build()

var fishology: Fishology? = null
    private set

val dataDir: Path get() = FabricLoader.getInstance().gameDir.resolve(MOD_ID)

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
