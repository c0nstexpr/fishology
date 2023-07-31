package org.c0nstexpr.fishology.core

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.c0nstexpr.fishology.core.log.LogBuilder
import org.c0nstexpr.fishology.core.log.MCMessageWriter
import org.c0nstexpr.fishology.core.log.addMCWriter
import org.c0nstexpr.fishology.core.log.greeting
import org.c0nstexpr.fishology.core.log.removeWriterWhere

const val modId = "fishology-core"
const val modName = "Fishology Core"

internal val logger = LogBuilder().apply { tag = modId }.build()

fun init() = ClientLifecycleEvents.CLIENT_STARTED.register {
    val config = logger.mutableConfig

    config.addMCWriter(it.inGameHud.chatHud)

    logger.greeting()

    ClientLifecycleEvents.CLIENT_STOPPING.register {
        config.removeWriterWhere { writer -> writer is MCMessageWriter }
    }
}
