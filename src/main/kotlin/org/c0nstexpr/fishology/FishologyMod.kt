package org.c0nstexpr.fishology

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val modID = "fishology"
const val modName = "Fishology"

val logger: Logger = LoggerFactory.getLogger(modName)

lateinit var fishology: Fishology
    private set

/**
 * entry point defined in fabric.mod.json
 */
@Suppress("unused")
fun init() {
    ClientLifecycleEvents.CLIENT_STARTED.register {
        logger.info("Initializing $modName")

        fishology = Fishology(it)
        ClientLifecycleEvents.CLIENT_STOPPING.register { fishology.dispose() }
    }
}
