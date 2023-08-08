package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.c0nstexpr.fishology.config.ConfigControl
import org.c0nstexpr.fishology.log.LogBuilder
import org.c0nstexpr.fishology.log.MCMessageWriter
import org.c0nstexpr.fishology.log.greeting
import org.c0nstexpr.fishology.log.removeWriterWhere

const val coreModId = "fishology-core"
const val coreModName = "Fishology Core"

internal val CoreLogger = LogBuilder().apply { tag = coreModId }.build()

internal fun init() = ClientLifecycleEvents.CLIENT_STARTED.register {
    val loggerConfig = CoreLogger.mutableConfig
    CoreLogger.greeting()
    ConfigControl.init()
    ClientLifecycleEvents.CLIENT_STOPPING.register {
        loggerConfig.removeWriterWhere { writer -> writer is MCMessageWriter }
    }
}
