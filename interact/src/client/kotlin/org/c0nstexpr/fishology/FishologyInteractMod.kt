package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.c0nstexpr.fishology.log.LogBuilder
import org.c0nstexpr.fishology.log.greeting

const val modId = "fishology"
const val modName = "Fishology"

internal val logger = LogBuilder().apply { tag = modId }.build()

var fishology: Fishology? = null
    private set

internal fun init() {
    logger.greeting()

    ClientPlayConnectionEvents.JOIN.register { handler, _, client ->
        if (fishology?.isDisposed != false) fishology = Fishology(client, handler)

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            fishology?.dispose()
            fishology = null
        }
    }
}
