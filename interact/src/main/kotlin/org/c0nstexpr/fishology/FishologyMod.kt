package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.c0nstexpr.fishology.core.log.LogBuilder
import org.c0nstexpr.fishology.core.log.MCMessageWriter
import org.c0nstexpr.fishology.core.log.addMCWriter
import org.c0nstexpr.fishology.core.log.greeting
import org.c0nstexpr.fishology.core.log.removeWriterWhere
import org.c0nstexpr.fishology.core.modId

const val modId = "fishology"
const val modName = "Fishology"

internal val logger = LogBuilder().apply { tag = modId }.build()

var fishology: Fishology? = null
    private set

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    logger.greeting()

    ClientPlayConnectionEvents.JOIN.register { handler, _, client ->
        logger.mutableConfig.addMCWriter(client)

        if (fishology?.isDisposed != false) fishology = Fishology(client, handler)

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            fishology?.dispose()
            fishology = null
            logger.mutableConfig.removeWriterWhere { writer -> writer is MCMessageWriter }
        }
    }
}
