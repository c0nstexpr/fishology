package org.c0nstexpr.fishology.core

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.apache.logging.log4j.kotlin.logger
import org.c0nstexpr.fishology.core.log.MCMessageLogger
import org.c0nstexpr.fishology.core.log.greeting


const val modID = "fishology-core"
const val modName = "Fishology Core"

val logger = logger(modID)

fun init() {
    ClientLifecycleEvents.CLIENT_STARTED.register {
        val appender = MCMessageLogger.Builder().build()

        logger.delegate.

        logger.greeting()
    }
}
