package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.c0nstexpr.fishology.log.LogBuilder
import java.nio.file.Path
import org.c0nstexpr.fishology.log.greeting as greeting1

const val modId = "fishology"
const val modName = "Fishology"

internal val logger = LogBuilder().apply { tag = modId }.build()

var fishology: Fishology? = null
    private set

val dataDir: Path get() = FabricLoader.getInstance().gameDir.resolve(modId)

internal fun init() {
    logger.greeting1()

    ClientPlayConnectionEvents.JOIN.register { _, _, client ->
        if (fishology?.isDisposed != false) fishology = Fishology(client)

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            fishology?.dispose()
            fishology = null
        }
    }
}
